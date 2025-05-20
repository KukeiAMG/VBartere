package com.example.ChatService.repository;

import com.example.ChatService.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderAndRecipientOrRecipientAndSenderOrderByTimestampAsc(
            Long sender1, Long recipient1, Long sender2, Long recipient2);

    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.chatId = :chatId")
    void deleteByChatId(@Param("chatId") Long chatId);
} 