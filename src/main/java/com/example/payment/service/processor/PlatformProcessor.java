package com.example.payment.service.processor;

import com.example.payment.model.Transaction;
import com.example.payment.model.TransactionType;
import com.example.payment.model.User;
import com.example.payment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PlatformProcessor {
    private static final Logger log = LoggerFactory.getLogger(PlatformProcessor.class);
    private static final Long PLATFORM_USER_ID = 0L;
    private final UserRepository userRepository;

    public PlatformProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User processPlatformCommission(BigDecimal platformAmount){
        validatePlatformRequest(platformAmount);
        User platformAccount = getOrCreatePlatformAccount();
        platformAccount.setBalance(platformAccount.getBalance().add(platformAmount));
        return platformAccount;
    }

    private void validatePlatformRequest(BigDecimal platformAmount) {
        if (platformAmount == null || platformAmount.compareTo(BigDecimal.valueOf(0)) < 0) {
            throw new IllegalArgumentException("Некорректные входные данные");
        }
    }
    @Transactional(readOnly = true)
    private User getOrCreatePlatformAccount() {
        return userRepository.findById(PLATFORM_USER_ID)
                .orElseGet(() -> {
                    User newAccount = new User();
                    newAccount.setUserId(PLATFORM_USER_ID);
                    newAccount.setBalance(BigDecimal.ZERO);
                    newAccount.setCreated(LocalDateTime.now());
                    return newAccount;
                });
    }

    public Transaction createPlatformTransaction(BigDecimal platformAmount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(PLATFORM_USER_ID);
        transaction.setAmount(platformAmount);
        transaction.setType(TransactionType.CREDIT);
        transaction.setReason("Platform commission: " + platformAmount);
        transaction.setOperationId(UUID.randomUUID().toString());
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}
