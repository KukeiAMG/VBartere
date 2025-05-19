package com.vbartere.Advertisement.kafka.Service.Producers.Admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SendAdminService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String TOPIC = "administration.advertisement.event";

    public SendAdminService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Async("taskExecutor")
    public void sendAdminRequest(String advertisementDTO) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, advertisementDTO);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("ошибка при отправке сообщения: " + ex.getMessage());
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                System.out.printf("сообщение отправлено в ",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    public void updateAdminAsync(AdminAdvertisementDTO advertisementDTO) {
        try {
            String json = objectMapper.writeValueAsString(advertisementDTO);
            sendAdminRequest(json);
        } catch (Exception e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
        }
    }
}
