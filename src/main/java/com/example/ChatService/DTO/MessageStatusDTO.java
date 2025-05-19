package com.example.ChatService.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;


public class MessageStatusDTO {

    @NotBlank(message = "ID чат-комнаты не может быть пустым")
    private String chatRoomId;
    @NotEmpty(message = "Список ID сообщений не может быть пустым")
    private List<Long> messageIds;

    public MessageStatusDTO(String chatRoomId, List<Long> messageIds) {
        this.chatRoomId = chatRoomId;
        this.messageIds = messageIds;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }

    @Override
    public String toString() {
        return "MessageStatusDTO{" +
                "chatRoomId='" + chatRoomId + '\'' +
                ", messageIds=" + messageIds +
                '}';
    }
}