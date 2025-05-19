package com.example.ChatService.model;

import com.example.ChatService.DTO.ChatMessageDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;



import java.time.LocalDateTime;

/**
 * Сущность сообщения в чате.
 * Представляет собой отдельное сообщение, отправленное пользователем в чат-комнату.
 */
@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "ID чат-комнаты не может быть пустым")
    @Column(nullable = false)
    private String chatRoomId;

    @NotBlank(message = "ID отправителя не может быть пустым")
    @Column(nullable = false)
    private String senderId;

    @Size(max = 2000, message = "Длина сообщения не может превышать 2000 символов")
    @Column(length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean isRead = false;  // По умолчанию сообщение не прочитано

    public ChatMessage() {
    }

    public ChatMessage(String chatRoomId, String senderId, String content, LocalDateTime timestamp, boolean isRead) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    /**
     * Конструктор для создания сообщения из DTO.
     * По умолчанию сообщение помечается как непрочитанное.
     */
    public ChatMessage(ChatMessageDTO dto) {
        this(dto.getChatRoomId(), 
             dto.getSenderId(), 
             dto.getContent(), 
             LocalDateTime.now(),
             false);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", chatRoomId='" + chatRoomId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                '}';
    }
}