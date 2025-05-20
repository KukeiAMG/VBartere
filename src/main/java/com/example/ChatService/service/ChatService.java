package com.example.ChatService.service;

import com.example.ChatService.model.ChatMessage;
import com.example.ChatService.model.ChatRoom;
import com.example.ChatService.repository.ChatMessageRepository;
import com.example.ChatService.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Сервис для обработки сообщений чата.
 * Обеспечивает сохранение сообщений в базу данных, отправку через WebSocket
 * и публикацию событий в Kafka.
 * 
 * @author Your Name
 * @version 1.0
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserService userService;
    private final Validator validator;
    private final ObjectMapper objectMapper;
    private final ChatRoomService chatRoomService;  
    private final ChatRoomRepository chatRoomRepository;

    public ChatService(ChatMessageRepository messageRepository,
                       SimpMessagingTemplate messagingTemplate,
                       KafkaTemplate<String, String> kafkaTemplate,
                       UserService userService, 
                       ObjectMapper objectMapper, 
                       ChatRoomService chatRoomService,
                       ChatRoomRepository chatRoomRepository) 
    {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.chatRoomService = chatRoomService;
        this.chatRoomRepository = chatRoomRepository;

        // Инициализация валидатора
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Отправляет сообщение получателю.
     * Процесс отправки включает:
     * 1. Валидацию сообщения
     * 2. Проверку существования получателя
     * 3. Сохранение сообщения в базу данных
     * 4. Отправку сообщения получателю через WebSocket
     * 5. Публикацию события в Kafka
     * 
     * @param message сообщение для отправки
     * @throws IllegalArgumentException если сообщение невалидно или получатель не существует
     * @throws RuntimeException если произошла ошибка при отправке
     */
    @Transactional
    public void sendMessage(ChatMessage message) {
        try {
            if (message.getTimestamp() == null) {
                message.setTimestamp(LocalDateTime.now());
            }
            
            // Добавим логирование для отладки
            logger.info("ChatService---Attempting to send message from {} to {}", 
                message.getSender(), message.getRecipient());
                
            // Проверяем существование отправителя
            if (!userService.userExists(message.getSender())) {
                logger.error("ChatService---Sender with ID {} does not exist", message.getSender());
                throw new IllegalArgumentException("Sender does not exist");
            }
    
            // Проверяем существование получателя
            if (!userService.userExists(message.getRecipient())) {
                logger.error("ChatService---Recipient with ID {} does not exist", message.getRecipient());
                throw new IllegalArgumentException("Recipient does not exist");
            }

            // Валидация сообщения
            Set<ConstraintViolation<ChatMessage>> violations = validator.validate(message);
            if (!violations.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("ChatService---Message validation failed: ");
                for (ConstraintViolation<ChatMessage> violation : violations) {
                    errorMessage.append(violation.getMessage()).append("; ");
                }
                logger.error(errorMessage.toString());
                throw new IllegalArgumentException(errorMessage.toString());
            }

            // Проверяем существование получателя
            if (!userService.userExists(message.getRecipient())) {
                logger.error("ChatService---Recipient with ID {} does not exist", message.getRecipient());
                throw new IllegalArgumentException("Recipient does not exist");
            }

            // Сохраняем сообщение в БД
            messageRepository.save(message);

            // Отправляем сообщение через WebSocket
            messagingTemplate.convertAndSend(
                "/topic/chat." + message.getChatId(),
                message
            );

            // Публикуем событие в Kafka
            kafkaTemplate.send("chat.messages", objectMapper.writeValueAsString(message));
            
            logger.info("ChatService---Message sent successfully from user {} to user {}",
                    message.getSender(), message.getRecipient());
        } catch (Exception e) {
            logger.error("ChatService---Error sending message: {}", e.getMessage());
        }
    }

    /**
     * Очищает историю сообщений в чате.
     * 
     * @param chatId ID чата для очистки
     * @param userId ID пользователя, запрашивающего очистку
     */
    @Transactional
    public void clearChatHistory(Long chatId, Long userId) {
    // Получаем чат и проверяем, является ли пользователь его участником
        ChatRoom chatRoom = chatRoomService.getChatRoomById(chatId);
        if (chatRoom == null || !chatRoomService.isUserInChat(chatId, userId)) {
          throw new IllegalArgumentException("Chat not found or user is not a participant");
        }

        // Удаляем все сообщения чата из базы данных
        messageRepository.deleteByChatId(chatId);
    
        // Отправляем уведомление об очистке чата всем участникам
        messagingTemplate.convertAndSend(
            "/topic/chat." + chatId,
            Map.of("type", "CLEAR_CHAT", "chatId", chatId)
        );
    }


    /**
     * Получает историю сообщений между двумя пользователями.
     * Сообщения возвращаются в хронологическом порядке.
     * 
     * @param user1Id ID первого пользователя
     * @param user2Id ID второго пользователя
     * @return список сообщений между пользователями
     */
    public List<ChatMessage> getChatHistory(Long user1Id, Long user2Id) {
        return messageRepository.findBySenderAndRecipientOrRecipientAndSenderOrderByTimestampAsc(
            user1Id, user2Id, user1Id, user2Id);
    }
} 