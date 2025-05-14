package com.example.ChatService.DTO;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String chatRoomId;
    private String senderId;
    private String content;
}
