package com.example.ChatService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки уведомлений в Kafka.
 * Используется для отправки уведомлений о создании чат-комнат.
 */
@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    // Константы для топиков
    public static final String TOPIC_CHAT_NOTIFICATIONS = "chat-notifications"; // Топик для уведомлений о создании комнат

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Отправляет объект в указанный топик Kafka, сериализуя его в JSON.
     * 
     * @param topic название топика
     * @param object объект для отправки
     * @throws RuntimeException если возникла ошибка при сериализации или отправке
     */
    public void sendObject(String topic, Object object) {
        try {
            String message = objectMapper.writeValueAsString(object);
            
            kafkaTemplate.send(topic, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        handleSuccessfulSend(topic, result.getRecordMetadata());
                    } else {
                        handleFailedSend(topic, ex);
                    }
                });
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации объекта для топика {}: {}", topic, e.getMessage());
            throw new RuntimeException("Ошибка сериализации объекта в JSON", e);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения в топик {}: {}", topic, e.getMessage());
            throw new RuntimeException("Ошибка отправки сообщения в Kafka", e);
        }
    }

    /**
     * Обработка успешной отправки сообщения.
     */
    private void handleSuccessfulSend(String topic, RecordMetadata metadata) {
        log.debug("Сообщение успешно отправлено в топик {}: partition={}, offset={}",
                topic, metadata.partition(), metadata.offset());
    }

    /**
     * Обработка ошибки отправки сообщения.
     */
    private void handleFailedSend(String topic, Throwable ex) {
        log.error("Ошибка при отправке сообщения в топик {}: {}", topic, ex.getMessage());
    }
}