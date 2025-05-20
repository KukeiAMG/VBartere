package com.example.ChatService.repository;

import com.example.ChatService.model.ChatRoom;
import com.example.ChatService.model.ChatRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "((cr.user1Id = :user1Id AND cr.user2Id = :user2Id) OR " +
           "(cr.user1Id = :user2Id AND cr.user2Id = :user1Id)) AND " +
           "cr.status = 'ACTIVE'")
    Optional<ChatRoom> findChatRoomBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "(cr.user1Id = :userId OR cr.user2Id = :userId) AND " +
           "cr.status = :status")
    List<ChatRoom> findUserChatRooms(@Param("userId") Long userId, @Param("status") ChatRoomStatus status);
    
    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END FROM ChatRoom cr " +
           "WHERE cr.id = :roomId AND (cr.user1Id = :userId OR cr.user2Id = :userId)")
    boolean existsByIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);
} 