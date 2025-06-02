package com.example.payment.repository;

import com.example.payment.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUserIdAndCreatedAtAfter(Long userId, LocalDateTime from);
    List<Transaction> findAllByUserId(Long userId);
} 