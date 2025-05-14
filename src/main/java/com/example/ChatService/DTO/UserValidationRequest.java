package com.example.ChatService.DTO;
import lombok.Data;
import java.time.Instant;

@Data // Добавьте lombok аннотацию
public class UserValidationRequest {
    private String userId;
    private Instant timestamp;

    public UserValidationRequest(String userId) {
        this.userId = userId;
        this.timestamp = Instant.now();
    }
}

