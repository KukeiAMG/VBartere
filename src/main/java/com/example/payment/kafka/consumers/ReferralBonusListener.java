package com.example.payment.kafka.consumers;

import com.example.payment.kafka.DTO.ReferralRewardsDistributionDTO;
import com.example.payment.service.ReferralOperationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.Map;

/**
 * Kafka Listener для обработки событий referral.bonus.payment
 * Слушает события из Kafka, десериализует их и вызывает соответствующие методы сервиса.
 */

@Component
public class ReferralBonusListener {
    private static final Logger log = LoggerFactory.getLogger(ReferralBonusListener.class);


    private final ObjectMapper objectMapper;
    private final ReferralOperationService referralOperationService;
    private final String TOPIC = "referral.bonus.payment";

    public ReferralBonusListener(ObjectMapper objectMapper, ReferralOperationService referralOperationService) {
        this.objectMapper = objectMapper;
        this.referralOperationService = referralOperationService;
    }

    /**
     * Обрабатывает событие referral.bonus.payment для начисления бонусов.
     * @param message JSON-строка с параметрами операции
     */
    @KafkaListener(topics = TOPIC)
    public void handleReferralBonus(String message) {
        try {
            ReferralRewardsDistributionDTO referralRewardsDistributionDTO = objectMapper.readValue(message, ReferralRewardsDistributionDTO.class);

            // Проверка на null значения
            if (referralRewardsDistributionDTO.getPlatformShare() == null || referralRewardsDistributionDTO.getDistribution() == null) {
                log.warn("Некорректные данные в referral.bonus.payment event: {}", message);
                return;
            }

            // Проверка на пустую карту распределения
            Map<Long, BigDecimal> distribution = referralRewardsDistributionDTO.getDistribution();
            if (distribution.isEmpty() && referralRewardsDistributionDTO.getPlatformShare() == null) {
                log.warn("Пустое распределение бонусов в referral.bonus.payment event: {}", message);
                return;
            }

            // Проверка на отрицательные значения
            if (referralRewardsDistributionDTO.getPlatformShare().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Отрицательная доля платформы в referral.bonus.payment event: {}", message);
                return;
            }

            for (Map.Entry<Long, BigDecimal> entry : distribution.entrySet()) {
                if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                    log.warn("Отрицательное значение бонусов для userId {} в referral.bonus.payment event: {}", 
                            entry.getKey(), message);
                    return;
                }
            }

            referralOperationService.referralOperation(referralRewardsDistributionDTO);
            log.info("Успешно обработано событие referral.bonus.payment: {}", referralRewardsDistributionDTO);
        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации referral.bonus.payment: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обработки referral.bonus.payment: {}", e.getMessage(), e);
        }
    }
}