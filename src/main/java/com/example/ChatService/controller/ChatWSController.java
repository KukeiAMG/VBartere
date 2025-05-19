package com.example.ChatService.controller;

import com.example.ChatService.DTO.ChatMessageDTO;
import com.example.ChatService.rateLimiter.RateLimitExceededException;
import com.example.ChatService.rateLimiter.RateLimiter;
import com.example.ChatService.model.ChatMessage;
import com.example.ChatService.repository.ChatMessageRepository;
import com.example.ChatService.service.KafkaProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket контроллер для обработки real-time сообщений в чате.
 * Обеспечивает функционал обмена сообщениями между пользователями,
 * отслеживание статуса пользователей и пакетное сохранение сообщений.
 *
 * @author YourName
 * @version 1.0
 */
@Controller
public class ChatWSController {
    private static final Logger logger = LoggerFactory.getLogger(ChatWSController.class);
    private static final int MAX_MESSAGES_PER_SECOND = 5;
    private static final long BATCH_FLUSH_INTERVAL = 500;
    private static final int BATCH_SIZE = 50;

    // Хранение статусов пользователей: Map<UserId, UserStatus>
    private final Map<String, String> userStatuses = new ConcurrentHashMap<>();

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RateLimiter rateLimiter;
    private final List<ChatMessage> messageBatch = new ArrayList<>();
    private final ScheduledExecutorService batchExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Конструктор WebSocket контроллера.
     *
     * @param chatMessageRepository репозиторий для сохранения сообщений
     * @param messagingTemplate шаблон для отправки WebSocket сообщений
     * @param rateLimiter сервис для ограничения частоты сообщений
     */
    public ChatWSController(ChatMessageRepository chatMessageRepository,
                          SimpMessagingTemplate messagingTemplate,
                          RateLimiter rateLimiter) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.rateLimiter = rateLimiter;

    }

    /**
     * Инициализация планировщика для пакетного сохранения сообщений.
     * Вызывается после создания бина.
     */
    @PostConstruct
    public void init() {
        batchExecutor.scheduleAtFixedRate(
                this::flushBatch,
                BATCH_FLUSH_INTERVAL,
                BATCH_FLUSH_INTERVAL,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Корректное завершение работы планировщика.
     * Вызывается перед уничтожением бина.
     */
    @PreDestroy
    public void cleanup() {
        batchExecutor.shutdown();
    }

    /**
     * Обработка подключения пользователя к WebSocket.
     * Устанавливает статус пользователя как 'ONLINE'.
     *
     * @param principal информация о пользователе
     */
    @MessageMapping("/chat.connect")
    public void handleConnect(Principal principal) {
        String userId = principal.getName();
        userStatuses.put(userId, "ONLINE");
        broadcastUserStatus(userId, "ONLINE");
    }

    /**
     * Обработка отключения пользователя от WebSocket.
     * Удаляет информацию о статусе пользователя и оповещает других.
     *
     * @param event событие отключения сессии
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event.getUser() != null) {
            String userId = event.getUser().getName();
            userStatuses.remove(userId);
            broadcastUserStatus(userId, "OFFLINE");
        }
    }

    /**
     * Обработка входящих сообщений чата.
     * Проверяет права, применяет ограничения и сохраняет сообщение.
     *
     * @param messageDto сообщение от клиента
     * @param principal информация об отправителе
     * @throws RateLimitExceededException если превышен лимит сообщений
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO messageDto, Principal principal) {
        String senderId = principal.getName();

        try {
            // 1. Валидация отправителя
            validateSender(senderId, messageDto.getSenderId());

            // 2. Проверка лимита сообщений
            rateLimiter.checkLimit(senderId, MAX_MESSAGES_PER_SECOND);

            // 3. Создание и буферизация сообщения
            final ChatMessage message = createMessageEntity(messageDto, senderId);
            addToBatch(message);

            // 4. Рассылка сообщения подписчикам
            broadcastMessage(messageDto);

        } catch (RateLimitExceededException e) { // обработка исключения лимита сообщений
            handleRateLimitExceeded(senderId, messageDto);
        } catch (Exception e) { // обработка других исключений
            handleGeneralError(senderId, e);
        }
    }

    /**
     * Обновление статуса пользователя.
     *
     * @param statusUpdate новый статус
     * @param principal информация о пользователе
     */
    @MessageMapping("/chat.status")
    public void updateStatus(@Payload Map<String, String> statusUpdate, Principal principal) {
        String userId = principal.getName();
        String newStatus = statusUpdate.get("status");
        
        if (newStatus != null && !newStatus.isEmpty()) {
            userStatuses.put(userId, newStatus);
            broadcastUserStatus(userId, newStatus);
        }
    }

    /**
     * Проверяет соответствие отправителя сообщения и авторизованного пользователя.
     *
     * @param actualSenderId ID авторизованного пользователя
     * @param dtoSenderId ID отправителя из сообщения
     * @throws SecurityException если ID не совпадают
     */
    private void validateSender(String actualSenderId, String dtoSenderId) {
        if (!actualSenderId.equals(dtoSenderId)) {
            logger.warn("Authorization failed for user: {}", actualSenderId);
            throw new SecurityException("Sender ID mismatch");
        }
    }

    /**
     * Создает сущность сообщения для сохранения в БД.
     *
     * @param dto DTO сообщения
     * @param senderId ID отправителя
     * @return сущность сообщения
     */
    private ChatMessage createMessageEntity(ChatMessageDTO dto, String senderId) {
        return new ChatMessage(
                dto.getChatRoomId(),
                senderId,
                trimContent(dto.getContent()),
                LocalDateTime.now(),
                false
        );
    }

    /**
     * Добавляет сообщение в пакет для последующего сохранения.
     * При достижении размера пакета выполняет сохранение.
     *
     * @param message сообщение для сохранения
     */
    private synchronized void addToBatch(ChatMessage message) {
        messageBatch.add(message);
        if (messageBatch.size() >= BATCH_SIZE) {
            flushBatch();
        }
    }

    /**
     * Сохраняет накопленные сообщения в БД.
     * Очищает буфер после сохранения.
     */
    private synchronized void flushBatch() {
        if (!messageBatch.isEmpty()) {
            try {
                chatMessageRepository.saveAll(messageBatch);
                logger.info("Flushed {} messages to DB", messageBatch.size());
                messageBatch.clear();
            } catch (Exception e) {
                logger.error("Batch save failed", e);
            }
        }
    }

    /**
     * Рассылает сообщение всем подписчикам комнаты.
     *
     * @param dto сообщение для рассылки
     */
    private void broadcastMessage(ChatMessageDTO dto) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + dto.getChatRoomId(),
                dto
        );
    }

    /**
     * Рассылает обновление статуса пользователя.
     *
     * @param userId ID пользователя
     * @param status новый статус
     */
    private void broadcastUserStatus(String userId, String status) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STATUS_CHANGE");
        message.put("userId", userId);
        message.put("status", status);
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/status", message);
    }

    /**
     * Обработка превышения лимита сообщений.
     *
     * @param userId ID пользователя
     * @param dto сообщение, вызвавшее превышение
     */
    private void handleRateLimitExceeded(String userId, ChatMessageDTO dto) {
        logger.warn("Rate limit exceeded for user: {}", userId);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/errors",
                Map.of("error", "Rate limit exceeded", "messageId", dto.getMessageId())
        );
    }

    /**
     * Обработка общих ошибок при отправке сообщений.
     *
     * @param userId ID пользователя
     * @param e исключение
     */
    private void handleGeneralError(String userId, Exception e) {
        logger.error("Processing error for user: {}", userId, e);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/errors",
                Map.of("error", "Message processing failed")
        );
    }

    /**
     * Обрезает содержимое сообщения до максимально допустимой длины.
     *
     * @param content исходный текст сообщения
     * @return обрезанный текст, если длина превышает лимит
     */
    private String trimContent(String content) {
        if (content.length() > 2000) {
            return content.substring(0, 2000);
        } else {
            return content;
        }
    }
}