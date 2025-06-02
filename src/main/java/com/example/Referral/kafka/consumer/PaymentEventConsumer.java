package com.example.Referral.kafka.consumer;

import com.example.Referral.kafka.DTO.PaymentEventDTO;
import com.example.Referral.service.ReferralCalculator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventConsumer {

    private final ReferralCalculator referralCalculator;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(ReferralCalculator referralCalculator, ObjectMapper objectMapper) {
        this.referralCalculator = referralCalculator;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment.calculatecomission.referral")
    public void listenPurchaseEvent(String paymentJSON){

        try {
            PaymentEventDTO paymentEventDTO = objectMapper.readValue(paymentJSON, PaymentEventDTO.class);
            referralCalculator.distributeReferralRewards(paymentEventDTO.getUserId(), paymentEventDTO.getCommission());
        } catch (JsonProcessingException e) {
            // Логируем ошибку, но не прерываем выполнение
            System.err.println("Ошибка при обработке сообщения: " + e.getMessage());
        } catch (Exception e) {
            // Ловим любые другие исключения, которые могут возникнуть
            System.err.println("Неожиданная ошибка при обработке сообщения: " + e.getMessage());
        }
    }
}
