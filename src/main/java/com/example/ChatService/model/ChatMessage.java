package com.example.ChatService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая сообщение в чате.
 * Хранит информацию об отправителе, получателе, содержимом и времени отправки сообщения.
 * 
 * @author Your Name
 * @version 1.0
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Sender cannot be empty")
    @Size(min = 3, max = 50, message = "Sender name must be between 3 and 50 characters")
    @Column(nullable = false, length = 50)
    private String sender;

    @NotBlank(message = "Recipient cannot be empty")
    @Size(min = 3, max = 50, message = "Recipient name must be between 3 and 50 characters")
    @Column(nullable = false, length = 50)
    private String recipient;

    @NotBlank(message = "Message content cannot be empty")
    @Size(min = 1, max = 4000, message = "Message content must be between 1 and 4000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "Timestamp cannot be null")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ChatMessage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
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

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
} 