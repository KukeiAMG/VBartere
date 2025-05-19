package com.example.ChatService.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO для получения команд от User Service через Kafka.
 * Используется для создания и удаления чат-комнат.
 */
public class UserEventDTO {
    public static final String EVENT_CREATE_CHAT = "CREATE_CHAT";
    public static final String EVENT_DELETE_CHAT = "DELETE_CHAT";

    @NotBlank(message = "Тип события не может быть пустым")
    private String eventType;

    @NotNull(message = "ID первого пользователя не может быть пустым")
    private Long userId;

    @NotNull(message = "ID второго пользователя не может быть пустым")
    private Long targetUserId;

    private LocalDateTime timestamp;

    public UserEventDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public UserEventDTO(String eventType, Long userId, Long targetUserId) {
        this();
        this.eventType = eventType;
        this.userId = userId;
        this.targetUserId = targetUserId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserEventDTO{" +
                "eventType='" + eventType + '\'' +
                ", userId='" + userId + '\'' +
                ", targetUserId='" + targetUserId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
