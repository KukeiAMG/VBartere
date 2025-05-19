package com.vbartere.userservice.Kafka.Producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SendAdminRequest {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String TOPIC = "administration.user.event";

    public SendAdminRequest(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Async("taskExecutor")
    public void sendAdminRequest(String adminUserDTO) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, adminUserDTO);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("ошибка при отправке сообщения: " + ex.getMessage());
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                System.out.println("сообщение отправлено в " + metadata.topic());
            }
        });
    }

    public void updateAdminAsync(AdminUserDTO adminUserDTO) {
        try {
            String json = objectMapper.writeValueAsString(adminUserDTO);
        } catch (JsonProcessingException e) {
            System.err.println("ошибка отправки сообщения: " + e.getMessage());
        }
    }
}
