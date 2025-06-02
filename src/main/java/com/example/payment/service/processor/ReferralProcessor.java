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
public class ReferralProcessor {
    private static final Logger log = LoggerFactory.getLogger(SellerProcessor.class);
    private final UserRepository userRepository;

    public ReferralProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User processReferral(Long userId, BigDecimal amount){
        validateReferralRequest(userId, amount);
        User referralAccount = getReferralAccount(userId);
        referralAccount.setBalance(referralAccount.getBalance().add(amount));
        return referralAccount;
    }

    private void validateReferralRequest(Long buyersId, BigDecimal amount) {
        if (buyersId == null || amount == null || amount.compareTo(BigDecimal.valueOf(0)) < 0) {
            throw new IllegalArgumentException("Некорректные входные данные");
        }
    }
    @Transactional(readOnly = true)
    private User getReferralAccount(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт реферала не найден ID: " + userId));
    }

    public Transaction createReferralTransaction(Long userId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.CREDIT);
        transaction.setReason("Referral bonus");
        transaction.setOperationId(UUID.randomUUID().toString());
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}
