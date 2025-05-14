package com.example.ChatService.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ChatRoom {
    @Id
    private String id;  // Например, "user1_user2"
    private String user1Id;  // ID первого пользователя (из user-service)
    private String user2Id;  // ID второго пользователя
    private LocalDateTime createdAt;
}
