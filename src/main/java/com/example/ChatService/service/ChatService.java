package com.example.ChatService.service;

import com.example.ChatService.DTO.ChatMessageDTO;
import com.example.ChatService.model.ChatMessage;
import com.example.ChatService.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage saveMessage(ChatMessageDTO chatMessageDTO){
        ChatMessage chatMessage = new ChatMessage(chatMessageDTO);
        return chatMessageRepository.save(chatMessage);
    }
}
