package com.example.payment.service;

import com.example.payment.kafka.DTO.ReferralRewardsDistributionDTO;
import com.example.payment.model.Transaction;
import com.example.payment.model.User;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.repository.UserRepository;
import com.example.payment.service.processor.PlatformProcessor;
import com.example.payment.service.processor.ReferralProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReferralOperationService {


    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ReferralProcessor referralProcessor;
    private final PlatformProcessor platformProcessor;

    private static final Logger log = LoggerFactory.getLogger(ReferralOperationService.class);

    public ReferralOperationService(UserRepository userRepository, TransactionRepository transactionRepository, ReferralProcessor referralProcessor, PlatformProcessor platformProcessor) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.referralProcessor = referralProcessor;
        this.platformProcessor = platformProcessor;
    }


    /**
     * Обрабатывает распределение реферальных бонусов между пользователями и платформой.
     * @param referralRewardsDistributionDTO DTO с распределением бонусов
     */
    @Transactional
    public void referralOperation(ReferralRewardsDistributionDTO referralRewardsDistributionDTO) {
        try {
            List<User> usersToUpdate = new ArrayList<>();
            List<Transaction> transactionsToCreate = new ArrayList<>();
            // Начисление бонусов пользователям
            for (Map.Entry<Long, BigDecimal> entry : referralRewardsDistributionDTO.getDistribution().entrySet()) {
                Long userId = entry.getKey();
                BigDecimal amount = entry.getValue();

                usersToUpdate.add(referralProcessor.processReferral(userId, amount));
                transactionsToCreate.add(referralProcessor.createReferralTransaction(userId, amount));

                log.info("Начислены реферальные бонусы пользователю {}: {}", userId, amount);
            }

            // Начисление бонусов платформе (используем специальный ID для платформы, например 0L)
            BigDecimal platformAmount = referralRewardsDistributionDTO.getPlatformShare();

            usersToUpdate.add(platformProcessor.processPlatformCommission(platformAmount));
            transactionsToCreate.add(platformProcessor.createPlatformTransaction(platformAmount));

            userRepository.saveAll(usersToUpdate);
            transactionRepository.saveAll(transactionsToCreate);
            log.info("Начислены реферальные бонусы платформе: {}", platformAmount);

        } catch (Exception e) {
            log.error("Ошибка при распределении реферальных бонусов: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process referral rewards distribution", e);
        }
    }
}
