package com.example.ChatService.controller;

import com.example.ChatService.DTO.ChatMessageDTO;
import com.example.ChatService.model.ChatMessage;
import com.example.ChatService.repository.ChatMessageRepository;
import com.example.ChatService.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatWSController {
    // Добавьте логгер
    private static final Logger logger = LoggerFactory.getLogger(ChatWSController.class);

    private final ChatMessageRepository chatMessageRepository;
    private final KafkaProducerService kafkaProducerService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWSController(ChatMessageRepository chatMessageRepository, KafkaProducerService kafkaProducerService, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO messageDto, Principal principal) {
        try {
            // Валидация отправителя
            String senderId = principal.getName();
            if (!senderId.equals(messageDto.getSenderId())) {
                logger.warn("Unauthorized message attempt from {}", senderId);
                throw new RuntimeException("Unauthorized");
            }

            // Сохранение сообщения
            ChatMessage message = new ChatMessage();
            message.setChatRoomId(messageDto.getChatRoomId());
            message.setSenderId(senderId);
            message.setContent(messageDto.getContent());
            message.setTimestamp(LocalDateTime.now());
            message.setRead(false);

            chatMessageRepository.save(message);

            // Отправка в Kafka
            kafkaProducerService.sendMessage("chat-messages", messageDto.toString());

            // Рассылка подписчикам
            messagingTemplate.convertAndSend("/topic/room/" + messageDto.getChatRoomId(), messageDto);

        } catch (Exception e) {
            logger.error("Error processing message", e);
            throw e;
        }
    }
}