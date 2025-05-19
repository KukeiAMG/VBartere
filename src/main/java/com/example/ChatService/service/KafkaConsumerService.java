package com.example.ChatService.service;

import com.example.ChatService.DTO.UserEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Сервис для обработки команд от User Service.
 * Слушает топик user-chat и обрабатывает команды создания/удаления чат-комнат.
 */
@Service
public class KafkaConsumerService {
    
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    public static final String TOPIC_USER_CHAT = "user-chat";
    
    private final ChatRoomService chatRoomService;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(ChatRoomService chatRoomService, ObjectMapper objectMapper) {
        this.chatRoomService = chatRoomService;
        this.objectMapper = objectMapper;
    }

    /**
     * Обрабатывает команды от User Service.
     * Поддерживает:
     * - Создание чат-комнаты (EVENT_CREATE_CHAT)
     * - Удаление чат-комнаты (EVENT_DELETE_CHAT)
     *
     * @param message JSON сообщение с командой
     */
    @KafkaListener(topics = TOPIC_USER_CHAT)
    @Retryable( // Повторная обработка ошибок
        value = {JsonProcessingException.class}, // Тип ошибки, которая будет повторно обрабатываться
        maxAttempts = 3, // Максимальное количество повторных попыток
        backoff = @Backoff(delay = 1000, multiplier = 2) // Задержка между повторными попытками и множитель для увеличения задержки
    )
    public void processCommand(String message) throws JsonProcessingException {
        if (message == null || message.trim().isEmpty()) {
            log.error("Получена пустая команда");
            return;
        }

        try {
            UserEventDTO command = objectMapper.readValue(message, UserEventDTO.class);
            
            if (!isValidCommand(command)) {
                log.error("Получена некорректная команда: отсутствуют обязательные поля");
                return;
            }

            log.info("Получена команда: type={}, userId={}, targetUserId={}", 
                    command.getEventType(), command.getUserId(), command.getTargetUserId());

            executeCommand(command);
            
        } catch (JsonProcessingException e) {
            log.error("Ошибка при разборе JSON команды: {}", e.getMessage());
            throw e; // Для ретрая
        } catch (Exception e) {
            log.error("Ошибка при выполнении команды: {}", e.getMessage(), e);
            // Не делаем throw тут, так как это может быть бизнес-ошибка
        }
    }

    /**
     * Проверяет валидность команды
     */
    private boolean isValidCommand(UserEventDTO command) {
        if (command.getEventType() == null || 
            command.getUserId() == null || 
            command.getTargetUserId() == null) {
            log.info(command.toString());
            return false;
        }

        // Проверяем что тип команды поддерживается
        return command.getEventType().equals(UserEventDTO.EVENT_CREATE_CHAT) ||
               command.getEventType().equals(UserEventDTO.EVENT_DELETE_CHAT);
    }

    /**
     * Выполняет команду в соответствующем сервисе
     */
    private void executeCommand(UserEventDTO command) {
        switch (command.getEventType()) {
            case UserEventDTO.EVENT_CREATE_CHAT:
                chatRoomService.createChatRoom(command.getUserId(), command.getTargetUserId());
                break;
            case UserEventDTO.EVENT_DELETE_CHAT:
                chatRoomService.deleteChatRoom(command.getUserId(), command.getTargetUserId());
                break;
            default:
                log.warn("Неподдерживаемый тип команды: {}", command.getEventType());
        }
    }
}