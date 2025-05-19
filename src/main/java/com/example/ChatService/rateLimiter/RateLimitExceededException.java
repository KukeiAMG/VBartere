package com.example.ChatService.rateLimiter;

/**
 * Исключение, выбрасываемое при превышении лимита запросов пользователем.
 * Используется в механизме ограничения частоты запросов (rate limiting).
 */
public class RateLimitExceededException extends RuntimeException {
    
    /**
     * Создает новое исключение с указанным сообщением
     * @param message сообщение об ошибке
     */
    public RateLimitExceededException(String message) {
        super(message);
    }
}
