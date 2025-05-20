package com.example.ChatService.controller;

import com.example.ChatService.model.ChatMessage;
import com.example.ChatService.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate; // Добавлено

    public ChatController(
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate // Внедрите зависимость
    ) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message, Principal principal) {
        try {
            Long senderId = Long.parseLong(principal.getName());
            message.setSender(senderId);

            // Сохраняем сообщение
            chatService.sendMessage(message);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid sender ID format");
        }
    }

    @MessageMapping("/chat.history")
    @SendToUser("/queue/chat.history")
    public List<ChatMessage> getChatHistory(Principal principal, @Payload String recipient) {
        try {
            Long senderId = Long.parseLong(principal.getName());
            Long recipientId = Long.parseLong(recipient);
            return chatService.getChatHistory(senderId, recipientId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }

    @MessageMapping("/chat.clear")
    public void clearChatHistory(@Payload Long chatId, Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            chatService.clearChatHistory(chatId, userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }
} 