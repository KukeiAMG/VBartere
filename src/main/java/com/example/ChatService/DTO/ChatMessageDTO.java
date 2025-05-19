package com.example.ChatService.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи сообщений через WebSocket.
 * Используется для отправки и получения сообщений в чате.
 */
public class ChatMessageDTO {
    
    private Long messageId;  // ID сообщения (опционально, используется при ответе)
    
    @NotBlank(message = "ID чат-комнаты не может быть пустым")
    private String chatRoomId;
    
    @NotBlank(message = "ID отправителя не может быть пустым")
    private String senderId;
    
    @NotBlank(message = "Сообщение не может быть пустым")
    @Size(max = 2000, message = "Длина сообщения не может превышать 2000 символов")
    private String content;

    public ChatMessageDTO() {
    }
    
    /**
     * Конструктор для создания нового сообщения
     */
    public ChatMessageDTO(String chatRoomId, String senderId, String content) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
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

    @Override
    public String toString() {
        return "ChatMessageDTO{" +
                "messageId=" + messageId +
                ", chatRoomId='" + chatRoomId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
