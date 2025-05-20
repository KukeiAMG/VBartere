package com.example.ChatService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First user ID cannot be empty")
    @Column(name = "user1_id", nullable = false)
    private String user1Id;

    @NotBlank(message = "Second user ID cannot be empty")
    @Column(name = "user2_id", nullable = false)
    private String user2Id;

    @NotNull(message = "Room status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastActivityAt;

    public ChatRoom() {
        this.status = ChatRoomStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public ChatRoomStatus getStatus() {
        return status;
    }

    public void setStatus(ChatRoomStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastActivityAt = LocalDateTime.now();
    }

    // Дополнительный метод валидации
    public void validateUsers() {
        if (user1Id != null && user2Id != null && user1Id.equals(user2Id)) {
            throw new IllegalArgumentException("Cannot create chat room with the same user");
        }
    }
} 