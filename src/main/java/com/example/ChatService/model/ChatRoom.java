package com.example.ChatService.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Сущность чат-комнаты.
 * Представляет собой приватную комнату для общения между двумя пользователями.
 * ID комнаты формируется как композиция ID пользователей в ChatRoomService
 */
@Entity
@Table(name = "chat_room", indexes = {
    @Index(name = "idx_user1", columnList = "user1Id"),
    @Index(name = "idx_user2", columnList = "user2Id")
})
public class ChatRoom {
    
    @Id
    private String id;  // Формируется как "user1Id_user2Id" (отсортированные)
    
    @NotBlank
    @Column(nullable = false)
    private String user1Id;  // ID первого пользователя (из user-service)
    
    @NotBlank
    @Column(nullable = false)
    private String user2Id;  // ID второго пользователя
    
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ChatRoom() {
    }

    public ChatRoom(String id, String user1Id, String user2Id, LocalDateTime createdAt) {
        this.id = id;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "id='" + id + '\'' +
                ", user1Id='" + user1Id + '\'' +
                ", user2Id='" + user2Id + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
