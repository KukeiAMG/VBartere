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
        message.setSender(principal.getName());
        chatService.sendMessage(message);
    }

    @SubscribeMapping("/chat.history")
    public List<ChatMessage> getChatHistory(Principal principal, String recipient) {
        return chatService.getChatHistory(principal.getName(), recipient);
    }
} 