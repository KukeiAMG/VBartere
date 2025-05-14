package com.example.ChatService.controller;

import com.example.ChatService.DTO.CreateRoomRequest;
import com.example.ChatService.model.ChatMessage;
import com.example.ChatService.model.ChatRoom;
import com.example.ChatService.repository.ChatMessageRepository;
import com.example.ChatService.repository.ChatRoomRepository;
import com.example.ChatService.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService; // Feign-клиент к user-service

    public ChatController(ChatRoomRepository chatRoomRepository,
                          ChatMessageRepository chatMessageRepository,
                          UserService userService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
    }

    // Создать чат-комнату
    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request) {
        // 1. Асинхронная проверка пользователей через Kafka
        userService.validateUserExists(request.getUser1Id());
        userService.validateUserExists(request.getUser2Id());

        // 2. Генерация ID комнаты (user1_user2, отсортировано)
        String roomId = Stream.of(request.getUser1Id(), request.getUser2Id())
                .sorted()
                .collect(Collectors.joining("_"));

        // 3. Проверка существования комнаты
        if (chatRoomRepository.existsById(roomId)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Chat room already exists");
        }

        // 4. Сохранение комнаты
        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setUser1Id(request.getUser1Id());
        room.setUser2Id(request.getUser2Id());
        room.setCreatedAt(LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(chatRoomRepository.save(room));
    }

    // Получить список сообщений в комнате
    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessage> getMessages(@PathVariable String roomId, Pageable pageable) {
        return chatMessageRepository.findByChatRoomIdOrderByTimestampDesc(roomId, pageable);
    }

    // Получить список комнат пользователя
    @GetMapping("/users/{userId}/rooms")
    public List<ChatRoom> getUserRooms(@PathVariable String userId) {
        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);
    }

    private String generateRoomId(String user1Id, String user2Id) {
        return user1Id.compareTo(user2Id) < 0
                ? user1Id + "_" + user2Id
                : user2Id + "_" + user1Id;
    }
}