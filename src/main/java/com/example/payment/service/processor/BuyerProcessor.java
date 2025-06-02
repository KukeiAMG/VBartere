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
import java.util.*;

@Component
public class BuyerProcessor {
    private static final Logger log = LoggerFactory.getLogger(BuyerProcessor.class);
    private final UserRepository userRepository;

    public BuyerProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User processBuyer(Long buyersId, Map<Long, BigDecimal> advertisements){
        validatePaymentRequest(buyersId, advertisements);
        User buyerAccount = getBuyerAccount(buyersId);
        BigDecimal totalAmount = getTotalAmount(advertisements);
        validateBuyerBalance(buyerAccount,totalAmount);
        buyerAccount.setBalance(buyerAccount.getBalance().subtract(totalAmount));
        return buyerAccount;
    }

    private void validatePaymentRequest(Long buyersId, Map<Long, BigDecimal> advertisements) {
        if (buyersId == null || advertisements == null || advertisements.isEmpty()) {
            throw new IllegalArgumentException("Некорректные входные данные");
        }
    }

    private void validateBuyerBalance(User buyerAccount, BigDecimal totalAmount){
        if (buyerAccount.getBalance().compareTo(totalAmount) < 0) {
            log.warn("Недостаточно средств: требуется {}, доступно {}", totalAmount, buyerAccount.getBalance());
            throw new IllegalArgumentException("Недостаточно средств");
        }
    }

    @Transactional(readOnly = true)
    private User getBuyerAccount(Long buyersId) {
        return userRepository.findById(buyersId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт покупателя не найден"));
    }

    private BigDecimal getTotalAmount(Map<Long, BigDecimal> advertisements){
        return advertisements.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Long> getAdvertisementsList(Map<Long, BigDecimal> advertisements){
        return new ArrayList<>(advertisements.keySet());
    }

    public Transaction createBuyerTransaction(Long buyersId, Map<Long, BigDecimal> advertisements) {
        BigDecimal totalAmount = getTotalAmount(advertisements);
        Transaction transaction = new Transaction();
        transaction.setUserId(buyersId);
        transaction.setAmount(totalAmount);
        transaction.setType(TransactionType.DEBIT);
        transaction.setReason("Payment for advertisements: " + getAdvertisementsList(advertisements));
        transaction.setOperationId(UUID.randomUUID().toString());
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}
