package com.vbartere.userservice.DTO;

import java.time.LocalDateTime;

public class UserChatDTO {
    private String eventType; // CREATE_CHAT, DELETE_CHAT
    private Long targetUserId;
    private Long userId;
    private LocalDateTime timestamp;

    public UserChatDTO() {
    }

    public UserChatDTO(String eventType,Long userId, Long targetUserId,  LocalDateTime timestamp) {
        this.eventType = eventType;
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
