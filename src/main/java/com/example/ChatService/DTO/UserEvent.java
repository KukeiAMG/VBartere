package com.example.ChatService.DTO;

import lombok.Data;

@Data
public class UserEvent {
    private String eventType; // "USER_CREATED", "USER_DELETED"
    private String userId;
    private String email;
}