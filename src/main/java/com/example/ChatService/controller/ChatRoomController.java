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
            if (user1Id.equals(user2Id)) {
                throw new IllegalArgumentException("Нельзя создать чат с самим собой");
            }
            chatRoomService.createChatRoom(user1Id, user2Id);
            
            // Отправляем обновленный список чатов обоим пользователям
            List<ChatRoom> user1Rooms = chatRoomService.getUserChatRooms(user1Id);
            List<ChatRoom> user2Rooms = chatRoomService.getUserChatRooms(user2Id);
            
            messagingTemplate.convertAndSendToUser(
                user1Id.toString(),
                "/queue/chat.rooms",
                user1Rooms
            );
            
            messagingTemplate.convertAndSendToUser(
                user2Id.toString(),
                "/queue/chat.rooms",
                user2Rooms
            );
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

} 