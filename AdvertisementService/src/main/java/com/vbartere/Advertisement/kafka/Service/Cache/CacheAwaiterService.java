package com.vbartere.Advertisement.kafka.Service.Cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheAwaiterService {

    private final Map<Long, CompletableFuture<Advertisement>> cacheFutures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final RedisCommands<String, String> redisCommands;

    public CacheAwaiterService(ObjectMapper objectMapper, RedisCommands redisCommands) {
        this.objectMapper = objectMapper;
        this.redisCommands = redisCommands;
    }

    // Этот метод вызывается, когда получено уведомление о готовности кэша
    public void completeCache(Long advertisementId) throws JsonProcessingException {
        CompletableFuture<Advertisement> future = cacheFutures.remove(advertisementId);
        if (future != null) {
            try {
                String cacheKey = "advertisement:" + advertisementId;
                String cacheData = redisCommands.get(cacheKey);

                if (cacheData == null || cacheData.isBlank()) {
                    future.completeExceptionally(new RuntimeException("Кэш ещё не готов или пустой"));
                    return;
                }

                Advertisement advertisement = objectMapper.readValue(cacheData, Advertisement.class);
                System.out.println("Из completeCache: " + advertisement);
                future.complete(advertisement);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }
    }

    // Ожидание готовности кэша
    public CompletableFuture<Advertisement> awaitCache(Long advertisementId) {
        CompletableFuture<Advertisement> future = new CompletableFuture<>();
        cacheFutures.put(advertisementId, future);
        return future;
    }

}
