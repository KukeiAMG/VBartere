package com.example.payment.service;

import com.example.payment.DTO.BalanceDTO;
import com.example.payment.DTO.HistoryDTO;
import com.example.payment.kafka.DTO.UserCreditDTO;
import com.example.payment.model.*;
import com.example.payment.repository.UserRepository;
import com.example.payment.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class BonusService {
    private static final Logger log = LoggerFactory.getLogger(BonusService.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public BonusService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Получить баланс пользователя.
     * @param userId Long пользователя
     * @return BalanceDTO с балансом пользователя
     * @throws IllegalArgumentException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public BalanceDTO getBalance(Long userId) {
        try {
            User account = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            return new BalanceDTO(account.getUserId(), account.getBalance(), "BONUS");
        } catch (IllegalArgumentException e) {
            log.error("Ошибка получения баланса для userId {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Получить историю операций пользователя.
     * @param userId Long пользователя
     * @param from дата, начиная с которой возвращать операции (может быть null)
     * @return список HistoryDTO
     */
    @Transactional(readOnly = true)
    public List<HistoryDTO> getHistory(Long userId, LocalDate from) {
        try {
            List<Transaction> txs = (from != null)
                    ? transactionRepository.findAllByUserIdAndCreatedAtAfter(userId, from.atStartOfDay())
                    : transactionRepository.findAllByUserId(userId);
            return txs.stream().map(tx -> new HistoryDTO(
                    tx.getId(),
                    tx.getUserId(),
                    tx.getAmount(),
                    tx.getType().name(),
                    tx.getReason(),
                    tx.getCreatedAt()
            )).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка получения истории для userId {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Создать бонусный аккаунт, если его ещё нет.
     * @param userId Long пользователя
     */
    @Transactional
    public void createAccountIfNotExists(Long userId) {
        try {
            if (!userRepository.existsById(userId)) {
                User acc = new User();
                acc.setUserId(userId);
                acc.setBalance(BigDecimal.ZERO);
                acc.setCreated(LocalDateTime.now());
                userRepository.save(acc);
                log.info("Создан бонусный аккаунт для userId {}", userId);
            }
        } catch (Exception e) {
            log.error("Ошибка создания аккаунта для userId {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void creditBonusToUserId(UserCreditDTO userCreditDTO){
        User user = userRepository.findById(userCreditDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт покупателя не найден"));

        BigDecimal balance = user.getBalance();
        user.setBalance(balance.add(userCreditDTO.getAmount()));
        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.setUserId(user.getUserId());
        transaction.setAmount(userCreditDTO.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setReason("Пополнение баланса на " + userCreditDTO.getAmount());
        transaction.setOperationId(UUID.randomUUID().toString());
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

    }

}