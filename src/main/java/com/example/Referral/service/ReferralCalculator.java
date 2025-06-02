package com.example.Referral.service;

import com.example.Referral.DTO.ParentChainDTO;
import com.example.Referral.kafka.DTO.ReferralRewardsDistributionDTO;
import com.example.Referral.kafka.producer.SendPaymentRequest;
import com.example.Referral.model.UserNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.shaded.com.google.protobuf.ServiceException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReferralCalculator {
    /**
     * Распределяет указанную сумму между реферальными уровнями пользователя.
     *
     * @param userId ID пользователя, для которого строится цепочка предков.
     * @param amount Общая сумма для распределения (должна быть положительной).
     * @return Map с двумя ключами:
     *         - "distribution" (Map<Long, BigDecimal>) — распределение по userId (ключ: userId, значение: сумма).
     *         - "ownerShare" (BigDecimal) — остаток суммы, который не был распределен (сумма для владельца системы).
     * @throws IllegalArgumentException Если amount <= 0 или userId == null.
     */

    private final DataService dataService;
    private final ObjectMapper objectMapper;
    private final SendPaymentRequest sendPaymentRequest;

    public ReferralCalculator(DataService dataService, ObjectMapper objectMapper, SendPaymentRequest sendPaymentRequest) {
        this.dataService = dataService;
        this.objectMapper = objectMapper;
        this.sendPaymentRequest = sendPaymentRequest;
    }

    public void distributeReferralRewards(Long userId, BigDecimal amount) throws JsonProcessingException, ServiceException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        System.out.println("----distributeReferralRewards----");

        // Получаем цепочку предков
        List<ParentChainDTO> parentChain = dataService.getParentChainWithLevels(userId);
        System.out.println("Цепочка предков: " + parentChain);

        Map<Long, BigDecimal> distribution = new LinkedHashMap<>();
        BigDecimal remainingAmount = amount;

        // Процентное распределение по уровням (level -> процент)
        Map<Integer, BigDecimal> levelPercents = Map.of(
                1, new BigDecimal("0.15"),  // 15%
                2, new BigDecimal("0.10"), // 10%
                3, new BigDecimal("0.05"), // 5%
                4, new BigDecimal("0.05"), // 5%
                5, new BigDecimal("0.05"), // 5%
                6, new BigDecimal("0.01")  // 1% (для уровней 6-15)
        );

        ReferralRewardsDistributionDTO dto = new ReferralRewardsDistributionDTO();

        if (parentChain == null || parentChain.isEmpty()) {
            // Вся сумма идет платформе
            dto.setDistribution(new HashMap<>());
            dto.setPlatformShare(amount);
        } else {
            // Распределение
            for (ParentChainDTO parent : parentChain) {
                UserNode userNode = parent.getUserNode();
                int level = parent.getLevel();

                BigDecimal percent = level <= 5 ? levelPercents.get(level) : levelPercents.get(6);
                BigDecimal reward = amount.multiply(percent);

                distribution.put(userNode.getUserId(), reward);
                remainingAmount = remainingAmount.subtract(reward);
            }
            // Формируем результат
            dto.setDistribution(distribution);
            dto.setPlatformShare(remainingAmount);
        }

        System.out.println("Часть комиссии платформы: " + remainingAmount);

        sendPaymentRequest.sendPaymentRequest(objectMapper.writeValueAsString(dto));
        System.out.println(dto);
    }
}
