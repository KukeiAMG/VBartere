package com.example.ChatService.controller;

import com.example.ChatService.model.ChatRoom;
import com.example.ChatService.service.ChatRoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/chat-rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRoomController(ChatRoomService chatRoomService, SimpMessagingTemplate messagingTemplate) {
        this.chatRoomService = chatRoomService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.room.create")
    public void createChatRoom(@Payload String targetUserId, Principal principal) {
        try {
            Long user1Id = Long.parseLong(principal.getName());
            Long user2Id = Long.parseLong(targetUserId);
            chatRoomService.createChatRoom(user1Id, user2Id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }

    @MessageMapping("/chat.room.delete")
    public void deleteChatRoom(@Payload Long roomId, Principal principal) throws JsonProcessingException {
        try {
            Long userId = Long.parseLong(principal.getName());
            chatRoomService.deleteChatRoom(roomId, userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }

    @SubscribeMapping("/user/queue/chat.rooms")
    public List<ChatRoom> getUserChatRooms(Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            return chatRoomService.getUserChatRooms(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }

    @MessageMapping("/chat.rooms")
    public void getChatRooms(Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            List<ChatRoom> rooms = chatRoomService.getUserChatRooms(userId);
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/chat.rooms",
                rooms
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }

    // REST endpoints для HTTP запросов
    @PostMapping
    public ResponseEntity<ChatRoom> createChatRoomRest(
            @RequestParam String targetUserId,
            Principal principal) {
        try {
            Long user1Id = Long.parseLong(principal.getName());
            Long user2Id = Long.parseLong(targetUserId);
            ChatRoom room = chatRoomService.createChatRoom(user1Id, user2Id);
            return ResponseEntity.ok(room);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteChatRoomRest(
            @PathVariable Long roomId,
            Principal principal) throws JsonProcessingException {
        try {
            Long userId = Long.parseLong(principal.getName());
            chatRoomService.deleteChatRoom(roomId, userId);
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getUserChatRoomsRest(Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            List<ChatRoom> rooms = chatRoomService.getUserChatRooms(userId);
            return ResponseEntity.ok(rooms);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }
} 