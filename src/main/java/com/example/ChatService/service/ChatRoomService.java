package com.example.ChatService.service;

import com.example.ChatService.model.ChatRoom;
import com.example.ChatService.model.ChatRoomStatus;
import com.example.ChatService.repository.ChatRoomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ChatRoomService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomService.class);
    
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    
    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                           UserService userService,
                           KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.chatRoomRepository = chatRoomRepository;
        this.userService = userService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    @Transactional
    public ChatRoom createChatRoom(String user1Id, String user2Id) {
        // Проверяем существование пользователей
        if (!userService.userExists(user1Id) || !userService.userExists(user2Id)) {
            System.out.println("createChatRoom---" + user1Id);
            System.out.println("createChatRoom---" +user2Id);
            throw new IllegalArgumentException("ChatRoomService---One or both users do not exist");
        }
        
        // Проверяем, не существует ли уже чат между этими пользователями
        return chatRoomRepository.findChatRoomBetweenUsers(user1Id, user2Id)
                .orElseGet(() -> {
                    try {
                        return createNewChatRoom(user1Id, user2Id);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    
    @Transactional
    public void deleteChatRoom(Long roomId, String userId) throws JsonProcessingException {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        
        // Проверяем, является ли пользователь участником чата
        if (!chatRoomRepository.existsByIdAndUserId(roomId, userId)) {
            throw new IllegalArgumentException("User is not a participant of this chat");
        }
        
        room.setStatus(ChatRoomStatus.DELETED);
        chatRoomRepository.save(room);


        ChatRoomCreatedEvent chatRoomCreatedEvent = new ChatRoomCreatedEvent(room);
        // Отправляем уведомление об удалении чата
        kafkaTemplate.send("chat.notifications", objectMapper.writeValueAsString(chatRoomCreatedEvent));
    }
    
    public List<ChatRoom> getUserChatRooms(String userId) {
        return chatRoomRepository.findUserChatRooms(userId, ChatRoomStatus.ACTIVE);
    }
    
    private ChatRoom createNewChatRoom(String user1Id, String user2Id) throws JsonProcessingException {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUser1Id(user1Id);
        chatRoom.setUser2Id(user2Id);
        
        // Валидация
        
        Set<ConstraintViolation<ChatRoom>> violations = validator.validate(chatRoom);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid chat room data: " + violations);
        }
        
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        ChatRoomCreatedEvent chatRoomCreatedEvent = new ChatRoomCreatedEvent(savedRoom);
        // Отправляем уведомление о создании чата
        kafkaTemplate.send("chat.notifications", objectMapper.writeValueAsString(chatRoomCreatedEvent));
        
        return savedRoom;
    }
    
    // Вспомогательные классы для событий
    private static class ChatRoomCreatedEvent {
        private final Long roomId;
        private final String user1Id;
        private final String user2Id;
        
        public ChatRoomCreatedEvent(ChatRoom room) {
            this.roomId = room.getId();
            this.user1Id = room.getUser1Id();
            this.user2Id = room.getUser2Id();
        }
        
        // Getters
        public Long getRoomId() { return roomId; }
        public String getUser1Id() { return user1Id; }
        public String getUser2Id() { return user2Id; }
    }
    
    private static class ChatRoomDeletedEvent {
        private final Long roomId;
        private final String user1Id;
        private final String user2Id;
        
        public ChatRoomDeletedEvent(ChatRoom room) {
            this.roomId = room.getId();
            this.user1Id = room.getUser1Id();
            this.user2Id = room.getUser2Id();
        }
        
        // Getters
        public Long getRoomId() { return roomId; }
        public String getUser1Id() { return user1Id; }
        public String getUser2Id() { return user2Id; }
    }
} 