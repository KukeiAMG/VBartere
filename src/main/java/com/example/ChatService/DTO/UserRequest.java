package com.example.ChatService.DTO;

import lombok.Data;

@Data
public class UserRequest {
    private String requestType; // "CHECK_USER_EXISTS"
    private String userId;
}