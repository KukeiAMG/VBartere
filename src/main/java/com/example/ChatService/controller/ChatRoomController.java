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
        chatRoomService.createChatRoom(principal.getName(), targetUserId);
    }

    @MessageMapping("/chat.room.delete")
    public void deleteChatRoom(@Payload Long roomId, Principal principal) throws JsonProcessingException {
        chatRoomService.deleteChatRoom(roomId, principal.getName());
    }

    @SubscribeMapping("/user/queue/chat.rooms")
    public List<ChatRoom> getUserChatRooms(Principal principal) {
        return chatRoomService.getUserChatRooms(principal.getName());
    }

    @MessageMapping("/chat.rooms")
    public void getChatRooms(Principal principal) {
        List<ChatRoom> rooms = chatRoomService.getUserChatRooms(principal.getName());
        messagingTemplate.convertAndSendToUser(
            principal.getName(),
            "/queue/chat.rooms",
            rooms
        );
    }

    // REST endpoints для HTTP запросов
    @PostMapping
    public ResponseEntity<ChatRoom> createChatRoomRest(
            @RequestParam String targetUserId,
            Principal principal) {
        ChatRoom room = chatRoomService.createChatRoom(principal.getName(), targetUserId);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteChatRoomRest(
            @PathVariable Long roomId,
            Principal principal) throws JsonProcessingException {
        chatRoomService.deleteChatRoom(roomId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getUserChatRoomsRest(Principal principal) {
        List<ChatRoom> rooms = chatRoomService.getUserChatRooms(principal.getName());
        return ResponseEntity.ok(rooms);
    }
} 