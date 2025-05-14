package com.example.ChatService.model;

import com.example.ChatService.DTO.ChatMessageDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String chatRoomId;
    private String senderId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;



    public ChatMessage() {}

    public ChatMessage(String chatRoomId, String senderId, String content, LocalDateTime timestamp, boolean isRead) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public ChatMessage(ChatMessageDTO chatMessageDTO){
        this.chatRoomId = chatMessageDTO.getChatRoomId();
        this.senderId = chatMessageDTO.getSenderId();
        this.content = chatMessageDTO.getContent();
        this.timestamp = LocalDateTime.now();
    }

}