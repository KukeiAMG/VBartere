package com.vbartere.userservice.Kafka.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.UserRepository;
import com.vbartere.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumeAdminRequest {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ConsumeAdminRequest(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user.event")
    public void handleAminRequest(String message) {
        try {
            UserEvent userEvent = objectMapper.readValue(message, UserEvent.class);
            if (userEvent.getEvent() == UserEventType.USER_BANNED) {
                User user = userRepository.findById(userEvent.getId()).orElseThrow(
                        () -> new EntityNotFoundException("Пользователь не найден в БД")
                );
                if (!user.isBanned()) {
                    user.setBanned(true);
                    userRepository.save(user);
                    System.out.println("Пользователь с ID " + user.getId() + " забанен.");
                } else {
                    System.out.println("Пользователь с ID " + user.getId() + " уже был забанен.");
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
