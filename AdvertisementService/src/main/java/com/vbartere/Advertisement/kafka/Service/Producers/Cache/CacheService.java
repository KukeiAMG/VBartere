package com.vbartere.Advertisement.kafka.Service.Producers.Cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
public class CacheService {

    private final CacheAwaiterService cacheAwaiterService;
    private final AdvertisementService advertisementService;

    public CacheService(CacheAwaiterService cacheAwaiterService, AdvertisementService advertisementService) {
        this.cacheAwaiterService = cacheAwaiterService;
        this.advertisementService = advertisementService;
    }

    @KafkaListener(topics = "cache.ready", groupId = "redis.cache.service.group")
    public void onCacheReady(String advertisementID) {
        Long id = Long.parseLong(advertisementID);
        System.out.println("Кэш готов, вызываем completeCache(id): " + id);
        try {
            cacheAwaiterService.completeCache(id);
        } catch (JsonProcessingException e) {
            System.err.println("Ошибка в completeCache: " + e.getMessage());
        }
    }
}
