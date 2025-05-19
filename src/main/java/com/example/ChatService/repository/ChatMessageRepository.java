package com.example.ChatService.repository;

import com.example.ChatService.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Получает сообщения комнаты с пагинацией, отсортированные по времени
     */
    Page<ChatMessage> findByChatRoomIdOrderByTimestampDesc(String chatRoomId, Pageable pageable);
    
    /**
     * Удаляет все сообщения комнаты
     */
    void deleteByChatRoomId(String chatRoomId);
    
    /**
     * Помечает сообщения как прочитанные
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.id IN :messageIds")
    void markMessagesAsRead(List<Long> messageIds);
}