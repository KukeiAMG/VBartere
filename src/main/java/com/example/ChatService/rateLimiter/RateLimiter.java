package com.example.ChatService.rateLimiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Компонент для ограничения частоты запросов пользователей (rate limiting).
 * Использует механизм скользящего окна с периодическим сбросом счетчиков.
 * Все параметры могут быть настроены через application.properties.
 */
@Component
public class RateLimiter {
    //Словарь для хранения счетчиков запросов для каждого пользователя
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    //Планировщик для выполнения задач с задержкой
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${rate.limiter.reset.period:1}")
    private int resetPeriodSeconds;

    @Value("${rate.limiter.default.max.requests:10}")
    private int defaultMaxRequests;

    /**
     * Инициализирует планировщик сброса счетчиков.
     * Период сброса настраивается через свойство rate.limiter.reset.period
     */
    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::resetCounters, resetPeriodSeconds, resetPeriodSeconds, TimeUnit.SECONDS);
    }

    /**
     * Проверяет, не превышен ли лимит запросов для пользователя
     * @param userId ID пользователя
     * @param maxRequests максимальное количество запросов в период
     * @throws RateLimitExceededException если лимит превышен
     */
    public void checkLimit(String userId, int maxRequests) throws RateLimitExceededException {
        AtomicInteger counter = counters.computeIfAbsent(userId, k -> new AtomicInteger(0));
        if (counter.incrementAndGet() > maxRequests) {
            throw new RateLimitExceededException(
                String.format("Rate limit of %d requests per %d seconds exceeded for user %s", 
                    maxRequests, resetPeriodSeconds, userId)
            );
        }
    }

    /**
     * Проверяет лимит запросов с использованием значения по умолчанию
     * @param userId ID пользователя
     * @throws RateLimitExceededException если лимит превышен
     */
    public void checkLimit(String userId) throws RateLimitExceededException {
        checkLimit(userId, defaultMaxRequests);
    }

    /**
     * Сбрасывает все счетчики запросов
     */
    private void resetCounters() {
        counters.clear();
    }

    /**
     * Корректно завершает работу планировщика при остановке приложения
     */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

