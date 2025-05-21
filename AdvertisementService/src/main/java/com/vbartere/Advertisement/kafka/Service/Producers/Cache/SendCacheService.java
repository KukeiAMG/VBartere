package com.vbartere.Advertisement.kafka.Service.Producers.Cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SendCacheService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String TOPIC = "cache.advertisement";

    public SendCacheService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Async("taskExecutor")
    public void sendCacheRequest(String advertisementId) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, advertisementId);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("ошибка при отправке сообщения: " + ex.getMessage());
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                System.out.printf("Топик: %s, Партиция: %d, Оффсет: %d%n",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    public void updateCacheAsync(AdvertisementDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            sendCacheRequest(json); // асинхронно, потому что sendCacheRequest @Async
        } catch (Exception e) {
            System.err.println("Ошибка обновления кэша: " + e.getMessage());
        }
    }
}
