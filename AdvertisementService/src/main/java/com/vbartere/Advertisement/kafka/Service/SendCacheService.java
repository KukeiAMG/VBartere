package com.vbartere.Advertisement.kafka.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SendCacheService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SendCacheService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    public void sendCacheRequest(String advertisementId) {
        System.out.println("Отправка сообщения с ID: " + advertisementId);
        kafkaTemplate.send("cache-advertisement", advertisementId);
        System.out.println("Сообщение: " + advertisementId + " отправлено на " + "cache-advertisement");
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
