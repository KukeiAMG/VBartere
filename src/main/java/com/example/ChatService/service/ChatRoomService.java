package com.example.ChatService.service;

import com.example.ChatService.DTO.ChatMessageDTO;
import com.example.ChatService.DTO.ChatNotificationDTO;
import com.example.ChatService.model.ChatMessage;
import com.example.ChatService.model.ChatRoom;
import com.example.ChatService.repository.ChatMessageRepository;
import com.example.ChatService.repository.ChatRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Основной сервис чата.
 * Управляет чат-комнатами и сообщениями:
 * - Создание/удаление комнат
 * - Отправка/получение сообщений
 * - Управление историей сообщений
 */
@Service
public class ChatRoomService {
    
    private static final Logger log = LoggerFactory.getLogger(ChatRoomService.class);
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final KafkaProducerService kafkaProducerService;

    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                         ChatMessageRepository chatMessageRepository,
                         KafkaProducerService kafkaProducerService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    // === Методы для работы с комнатами ===

    /**
     * Создает новую чат-комнату между двумя пользователями.
     * Если комната уже существует, операция игнорируется.
     *
     * @param userId1 ID первого пользователя
     * @param userId2 ID второго пользователя
     * @return созданная комната или null, если комната уже существует
     * @throws IllegalArgumentException если ID пользователей некорректны
     */
    @Transactional
    public ChatRoom createChatRoom(Long userId1, Long userId2) {
        validateUserIds(userId1, userId2);
        String roomId = generateRoomId(userId1.toString(), userId2.toString());

        if (chatRoomRepository.existsById(roomId)) {
            log.info("Чат-комната уже существует: {}", roomId);
            return null;
        }

        ChatRoom room = new ChatRoom(
                roomId,
                userId1.toString(),
                userId2.toString(),
                LocalDateTime.now()
        );

        ChatRoom savedRoom = chatRoomRepository.save(room);
        log.info("Создана чат-комната: {}", roomId);
        
        // Отправка уведомления о создании комнаты
        sendRoomNotification(savedRoom, ChatNotificationDTO.TYPE_ROOM_CREATED);
        
        return savedRoom;
    }

    /**
     * Удаляет чат-комнату и все её сообщения.
     * Если комната не найдена, операция игнорируется.
     *
     * @param userId1 ID первого пользователя
     * @param userId2 ID второго пользователя
     * @throws IllegalArgumentException если ID пользователей некорректны
     */
    @Transactional
    public void deleteChatRoom(Long userId1, Long userId2) {
        validateUserIds(userId1, userId2);
        String roomId = generateRoomId(userId1.toString(), userId2.toString());
        
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Чат-комната не найдена: " + roomId));

        // Сначала удаляем все сообщения комнаты
        chatMessageRepository.deleteByChatRoomId(roomId);
        // Потом удаляем саму комнату
        chatRoomRepository.deleteById(roomId);
        
        // Отправляем уведомление об удалении комнаты
        sendRoomNotification(room, ChatNotificationDTO.TYPE_ROOM_DELETED);
        
        log.info("Удалена чат-комната и все её сообщения: {}", roomId);
    }

    /**
     * Получает список всех чат-комнат пользователя.
     */
    public List<ChatRoom> getUserChatRooms(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID пользователя не может быть пустым");
        }
        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);
    }

    // === Методы для работы с сообщениями ===

    /**
     * Сохраняет новое сообщение.
     * Проверяет существование комнаты и права доступа отправителя.
     */
    @Transactional
    public ChatMessage saveMessage(ChatMessageDTO messageDto) {
        String roomId = messageDto.getChatRoomId();
        String senderId = messageDto.getSenderId();

        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Чат-комната не найдена: " + roomId));

        // Проверяем что отправитель является участником комнаты
        if (!isUserInRoom(senderId, room)) {
            throw new SecurityException("Пользователь не является участником чат-комнаты");
        }

        ChatMessage message = new ChatMessage(
            messageDto.getChatRoomId(),
            messageDto.getSenderId(),
            messageDto.getContent(),
            LocalDateTime.now(),
            false
        );

        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.debug("Сохранено сообщение: roomId={}, senderId={}", roomId, senderId);
        
        return savedMessage;
    }

    /**
     * Получает историю сообщений чат-комнаты с пагинацией.
     */
    public Page<ChatMessage> getRoomMessages(String roomId, String userId, Pageable pageable) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Чат-комната не найдена: " + roomId));

        if (!isUserInRoom(userId, room)) {
            throw new SecurityException("Пользователь не является участником чат-комнаты");
        }

        return chatMessageRepository.findByChatRoomIdOrderByTimestampDesc(roomId, pageable);
    }

    /**
     * Помечает сообщения как прочитанные.
     */
    @Transactional
    public void markMessagesAsRead(String roomId, String userId, List<Long> messageIds) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Чат-комната не найдена: " + roomId));

        if (!isUserInRoom(userId, room)) {
            throw new SecurityException("Пользователь не является участником чат-комнаты");
        }

        chatMessageRepository.markMessagesAsRead(messageIds);
        log.debug("Помечены как прочитанные сообщения: roomId={}, messageIds={}", roomId, messageIds);
    }

    // === Вспомогательные методы ===

    /**
     * Отправляет уведомление о событии с чат-комнатой
     * @param room комната
     * @param eventType тип события (создание/удаление)
     */
    private void sendRoomNotification(ChatRoom room, String eventType) {
        ChatNotificationDTO notification = new ChatNotificationDTO(
            eventType,
            room.getId(),
            Arrays.asList(room.getUser1Id(), room.getUser2Id())
        );
        
        try {
            kafkaProducerService.sendObject(KafkaProducerService.TOPIC_CHAT_NOTIFICATIONS, notification);
            log.info("Отправлено уведомление {}: {}", eventType, room.getId());
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления {}: {}", eventType, e.getMessage(), e);
        }
    }

    /**
     * Генерирует ID комнаты из ID пользователей.
     * ID сортируются для обеспечения уникальности независимо от порядка параметров.
     */
    private String generateRoomId(String userId1, String userId2) {
        return Stream.of(userId1, userId2)
                .sorted()
                .collect(Collectors.joining("_"));
    }

    /**
     * Проверяет корректность ID пользователей
     */
    private void validateUserIds(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("ID пользователей не могут быть пустыми");
        }
        if (userId1.equals(userId2)) {
            throw new IllegalArgumentException("ID пользователей не могут совпадать");
        }
    }

    private boolean isUserInRoom(String userId, ChatRoom room) {
        return userId.equals(room.getUser1Id()) || userId.equals(room.getUser2Id());
    }
}
