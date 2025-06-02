package com.example.payment.kafka.consumers;

import com.example.payment.kafka.DTO.PaymentDTO;
import com.example.payment.kafka.DTO.UserCreditDTO;
import com.example.payment.kafka.DTO.UserIdDTO;
import com.example.payment.service.BonusService;
import com.example.payment.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.annotation.KafkaListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Kafka Listener для обработки событий user.payment.event
 * Слушает события из Kafka, десериализует их и вызывает соответствующие методы сервиса.
 */
@Component
public class UserActivityListener {
    private static final Logger log = LoggerFactory.getLogger(UserActivityListener.class);

    private final BonusService bonusService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final String TOPIC_REG = "user.payment.register";
    private final String TOPIC = "user.payment.event";
    private final String TOPIC_CREDIT = "user.payment.credit";

    public UserActivityListener(BonusService bonusService, PaymentService paymentService, ObjectMapper objectMapper) {
        this.bonusService = bonusService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    /**
     * Обрабатывает событие user.payment.event для создания бонусного аккаунта пользователя.
     * @param recordJSON JSON-строка с userId
     */
    @KafkaListener(topics = TOPIC_REG)
    public void handleUserRegister(String recordJSON) {
        try {
            UserIdDTO userIdDTO = objectMapper.readValue(recordJSON, UserIdDTO.class);
            System.out.println(userIdDTO);
            if (userIdDTO.getUserId() == null) {
                log.warn("userId is null in user.payment.event: {}", recordJSON);
                return;
            }
            bonusService.createAccountIfNotExists(userIdDTO.getUserId());
            log.info("Обработано событие user.payment.event для userId {}", userIdDTO.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации user.payment.event: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обработки user.payment.event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = TOPIC_CREDIT)
    public void handleUserCredit(String recordJSON){
        try{
            UserCreditDTO userCreditDTO = objectMapper.readValue(recordJSON, UserCreditDTO.class);
            if (userCreditDTO.getUserId() == null || userCreditDTO.getAmount() == null) {
                log.warn("userId или amount = null in user.payment.credit: {}", recordJSON);
                return;
            }
            bonusService.creditBonusToUserId(userCreditDTO);
            log.info("Обработано событие user.payment.credit для userId {}", userCreditDTO.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации user.payment.credit: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обработки user.payment.credit: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = TOPIC)
    public void handlePayment(String message) {
        try {
            PaymentDTO paymentDTO = objectMapper.readValue(message, PaymentDTO.class);

            System.out.println(paymentDTO);
            // Проверка на null значения
            if (paymentDTO.getBuyersId() == null ||
                    paymentDTO.getAdvertisementsWithPrice() == null) {
                log.warn("Некорректные данные в user.buy.payment event: {}", message);
                return;
            }

            // Проверка на пустую карту объявлений
            Map<Long, BigDecimal> advertisements = paymentDTO.getAdvertisementsWithPrice();
            if (advertisements.isEmpty()) {
                log.warn("Пустой список объявлений в user.buy.payment event: {}", message);
                return;
            }

            // Проверка на отрицательные значения в ценах
            for (Map.Entry<Long, BigDecimal> entry : advertisements.entrySet()) {
                if (entry.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn("Некорректная цена для объявления {} в user.buy.payment event: {}",
                            entry.getKey(), message);
                    return;
                }
            }

            // Обработка платежа
            paymentService.processPayment(
                    paymentDTO.getBuyersId(),
                    paymentDTO.getAdvertisementsWithPrice()
            );

        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации user.buy.payment: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации платежа: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обработки user.buy.payment: {}", e.getMessage(), e);
        }
    }
}
