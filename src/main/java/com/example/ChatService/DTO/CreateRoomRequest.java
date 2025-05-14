package com.example.ChatService.DTO;

import lombok.Data;

@Data
public class CreateRoomRequest {
    private String user1Id;
    private String user2Id;
}
