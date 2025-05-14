package com.example.ChatService.service;

import com.example.ChatService.DTO.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    @KafkaListener(topics = "user-events")
    public void handleUserEvent(UserEvent event) {
        if (event.getEventType().equals("USER_DELETED")) {
            // Удалить сообщения пользователя из БД
        }
    }
}