package com.example.ChatService.DTO;

import lombok.Data;

import java.util.List;

@Data
public class MarkAsReadRequest {
    private String chatRoomId;
    private List<Long> messageIds;
}