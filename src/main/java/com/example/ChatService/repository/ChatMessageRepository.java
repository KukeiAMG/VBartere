package com.example.ChatService.repository;

import com.example.ChatService.model.ChatMessage;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Найти все сообщения в комнате с пагинацией
    List<ChatMessage> findByChatRoomIdOrderByTimestampDesc(String chatRoomId, Pageable pageable);

    // Непрочитанные сообщения для пользователя в конкретной комнате
    List<ChatMessage> findByChatRoomIdAndSenderIdNotAndIsReadFalse(String chatRoomId, String userId);

    // Исправьте импорт Pageable
    List<ChatMessage> findByChatRoomIdOrderByTimestampDesc(String chatRoomId, org.springframework.data.domain.Pageable pageable);

    // Добавьте метод для пометки сообщений как прочитанных
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.id IN :messageIds")
    void markMessagesAsRead(@Param("messageIds") List<Long> messageIds);
}