package com.example.ChatService.service;

import com.example.ChatService.DTO.UserRequest;
import com.example.ChatService.DTO.UserValidationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {
    private final KafkaTemplate<String, UserValidationRequest> kafkaTemplate; // Исправлен тип

    // Добавьте конструктор
    public UserService(KafkaTemplate<String, UserValidationRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void validateUserExists(String userId) {
        UserValidationRequest request = new UserValidationRequest(userId);
        kafkaTemplate.send("user-validation-requests", request);
    }
}
