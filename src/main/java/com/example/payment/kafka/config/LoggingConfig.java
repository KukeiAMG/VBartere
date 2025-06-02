package com.example.payment.kafka.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Конфигурационный класс для настройки логирования в приложении.
 * Реализует механизм отслеживания запросов через traceId.
 *
 * @author VBartere
 * @version 1.0
 */
@Configuration
public class LoggingConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
    }

    /**
     * Внутренний класс для перехвата HTTP запросов и добавления traceId.
     * Реализует интерфейс HandlerInterceptor для обработки запросов.
     */
    public static class LoggingInterceptor implements HandlerInterceptor {
        /**
         * Обрабатывает запрос перед его выполнением.
         * Извлекает или генерирует traceId и добавляет его в MDC.
         *
         * @param request HTTP запрос
         * @param response HTTP ответ
         * @param handler обработчик запроса
         * @return true для продолжения обработки запроса
         */
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String traceId = request.getHeader("X-Trace-ID");
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }
            MDC.put("traceId", traceId);
            return true;
        }

        /**
         * Вызывается после завершения обработки запроса.
         * Очищает MDC контекст.
         *
         * @param request HTTP запрос
         * @param response HTTP ответ
         * @param handler обработчик запроса
         * @param ex исключение, если оно возникло
         */
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                  Object handler, Exception ex) {
            MDC.clear();
        }
    }
}