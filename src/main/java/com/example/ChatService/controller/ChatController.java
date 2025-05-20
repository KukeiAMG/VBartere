package com.example.ChatService.controller;

import com.example.ChatService.model.ChatMessage;
import com.example.ChatService.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message, Principal principal) {
        try {
            Long senderId = Long.parseLong(principal.getName());
            message.setSender(senderId);
            chatService.sendMessage(message);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid sender ID format");
        }
    }

    @SubscribeMapping("/chat.history")
    public List<ChatMessage> getChatHistory(Principal principal, String recipient) {
        try {
            Long senderId = Long.parseLong(principal.getName());
            Long recipientId = Long.parseLong(recipient);
            return chatService.getChatHistory(senderId, recipientId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }
} 