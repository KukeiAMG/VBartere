package com.example.ChatService.repository;

import com.example.ChatService.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    // Найти чат-комнату по ID двух пользователей (порядок не важен)
    Optional<ChatRoom> findByUser1IdAndUser2Id(String user1Id, String user2Id);
    Optional<ChatRoom> findByUser2IdAndUser1Id(String user1Id, String user2Id);

    // Найти все комнаты пользователя
    List<ChatRoom> findByUser1IdOrUser2Id(String userId, String userId2);
}