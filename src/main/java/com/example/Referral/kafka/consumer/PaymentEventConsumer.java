package com.example.Referral.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {
    @KafkaListener(topics = "payment-processed", groupId = "referral-service-group")
    public void handlePaymentProcessed(String paymentJSON) {

        //TODO: реализовать логику получения JSON от пеймент сервиса
        // Сейчас:
        // - Принимает сырой JSON как String (нет десериализации в DTO)
        // - Нет обработки ошибок парсинга
        // - Нет логирования входящих сообщений
    }
}
