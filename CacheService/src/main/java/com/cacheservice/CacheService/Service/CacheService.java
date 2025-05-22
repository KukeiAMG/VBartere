package com.cacheservice.CacheService.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.AdvertisementDTO;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final ObjectMapper objectMapper;
    private final RedisCommands<String, String> redisCommands;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CacheService(ObjectMapper objectMapper, RedisCommands<String, String> redisCommands, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.redisCommands = redisCommands;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "cache.advertisement", groupId = "cache-service-group")
    public void listenAdvertisementCache(String advertisementDTO) throws JsonProcessingException {
        AdvertisementDTO newAdvertisementDTO = objectMapper.readValue(advertisementDTO, AdvertisementDTO.class);
        System.out.println(objectMapper.writeValueAsString(newAdvertisementDTO));

        String cacheKey = "advertisement:" + newAdvertisementDTO.getId();
        String cacheData = objectMapper.writeValueAsString(newAdvertisementDTO);

        redisCommands.setex(cacheKey, 1500, cacheData);

        kafkaTemplate.send("cache-ready", newAdvertisementDTO.getId().toString());
    }
}
