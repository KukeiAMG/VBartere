package com.vbartere.Advertisement.kafka.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final CacheAwaiterService cacheAwaiterService;

    public CacheService(CacheAwaiterService cacheAwaiterService) {
        this.cacheAwaiterService = cacheAwaiterService;
    }

    @KafkaListener(topics = "cache-ready", groupId = "redis-cache-service-group")
    public void onCacheReady(String advertisementID) throws JsonProcessingException {
        Long id = Long.parseLong(advertisementID);
        System.out.println("Отправили в cacheAwaiterService.completeCache(id)" + id);
        cacheAwaiterService.completeCache(id);
    }

}
