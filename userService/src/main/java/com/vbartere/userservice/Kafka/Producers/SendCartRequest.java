package com.vbartere.userservice.Kafka.Producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import com.vbartere.Shared.Kafka.Events.CartEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SendCartRequest {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    final String TOPIC = "cart.events";

    public SendCartRequest(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Async("taskExecutor")
    public void sendRequest(String cartEvent) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, cartEvent);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("ошибка при отправке сообщения: " + ex.getMessage());
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                System.out.println("Сообщение отправлено в " + metadata.topic() +
                        ", partition: " + metadata.partition() +
                        ", offset: " + metadata.offset());
            }
        });
    }

    public void updateAdminAsync(CartEvent cartEvent) {
        try {
            String json = objectMapper.writeValueAsString(cartEvent);
            sendRequest(json);
        } catch (JsonProcessingException e) {
            System.err.println("ошибка отправки сообщения: " + e.getMessage());
        }
    }
}
