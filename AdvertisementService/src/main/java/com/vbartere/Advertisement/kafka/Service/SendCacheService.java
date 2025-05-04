package com.vbartere.Advertisement.kafka.Service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SendCacheService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public SendCacheService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCacheRequest(String advertisementId) {
        System.out.println("Отправка сообщения с ID: " + advertisementId);
        kafkaTemplate.send("cache-advertisement", String.valueOf(advertisementId));
        System.out.println("Сообщение: " + advertisementId + " отправлено на " + "cache-advertisement");
    }
}
