package com.example.ChatService.controller;

import com.example.ChatService.model.ChatRoom;
import com.example.ChatService.repository.ChatMessageRepository;
import com.example.ChatService.repository.ChatRoomRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления чат-комнатами и сообщениями.
 * Предоставляет API для получения истории сообщений и списка чатов пользователя.
 * Создание комнат происходит через Kafka при получении команды от User Service.
 *
 * @author KukeiAMG
 * @version 1.0
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Конструктор контроллера чата.
     *
     * @param chatRoomRepository репозиторий для работы с чат-комнатами
     * @param chatMessageRepository репозиторий для работы с сообщениями
     */
    public ChatController(ChatRoomRepository chatRoomRepository,
                          ChatMessageRepository chatMessageRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Получает историю сообщений для указанной чат-комнаты.
     * Проверяет, имеет ли текущий пользователь доступ к этой комнате.
     *
     * @param roomId ID комнаты
     * @param userId ID пользователя из заголовка запроса
     * @param pageable параметры пагинации
     * @return ResponseEntity со списком сообщений или сообщением об ошибке
     * @throws RuntimeException если комната не найдена
     * 
     * @apiNote Сообщения возвращаются в порядке убывания по времени создания
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable String roomId, @RequestHeader("User-Id") String userId, Pageable pageable) {
        // Проверяем, что пользователь имеет доступ к комнате
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
                

        // Проверяем, что userId совпадает с id одного из участников чата (user1Id или user2Id)
        // Если userId не совпадает ни с одним из участников - возвращаем ошибку 403 Forbidden
        if (!room.getUser1Id().equals(userId) && !room.getUser2Id().equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }
        
        return ResponseEntity.ok(
            chatMessageRepository.findByChatRoomIdOrderByTimestampDesc(roomId, pageable)
        );
    }

    /**
     * Получает список всех чат-комнат пользователя.
     * Возвращает комнаты, где пользователь является одним из участников.
     *
     * @param userId ID пользователя
     * @return ResponseEntity со списком комнат
     */
    @GetMapping("/users/{userId}/rooms")
    public ResponseEntity<List<ChatRoom>> getUserRooms(@PathVariable String userId) {
        return ResponseEntity.ok(
            chatRoomRepository.findByUser1IdOrUser2Id(userId, userId)
        );
    }
}