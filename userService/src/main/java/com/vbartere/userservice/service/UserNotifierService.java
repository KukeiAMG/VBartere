package com.vbartere.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.UserDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserNotifierService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public UserNotifierService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "user-notifier", groupId = "user-notifier-group")
    public void listenUserNotification(String user) throws JsonProcessingException {
        UserDTO newUserDTO = objectMapper.readValue(user, UserDTO.class);
        System.out.println(objectMapper.writeValueAsString(newUserDTO));

        String cacheKey = "advertisement:" + newUserDTO.getId();
        String cacheData = objectMapper.writeValueAsString(newUserDTO);

        kafkaTemplate.send("user-notifier", newUserDTO.getId().toString());
    }
}
