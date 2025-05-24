package com.vbartere.userservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.userservice.DTO.UserChatDTO;
import com.vbartere.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class UserChatKafkaController {
    private final UserService userService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    final String TOPIC_USER_CHAT = "user-chat";

    public UserChatKafkaController(UserService userService, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.userService = userService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/api/chatroom/{targetUserId}/add")
    public ResponseEntity<?> addChatRoom(
            @PathVariable Long targetUserId,
            @RequestHeader("Authorization") String jwtToken) {

        try {
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            Long userId = userService.getUserIdByToken(token);

            // Создаем структурированное событие
            UserChatDTO userChatDTO = new UserChatDTO(
                    "CREATE_CHAT",
                    userId,
                    targetUserId,
                    LocalDateTime.now()
            );

            kafkaTemplate.send(TOPIC_USER_CHAT, objectMapper.writeValueAsString(userChatDTO));

            return ResponseEntity.ok().body(Map.of(
                    "status", "request_sent",
                    "UserId", userId,
                    "targetUserId", targetUserId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "chat_request_failed",
                    "message", e.getMessage()
            ));
        }
    }
}
