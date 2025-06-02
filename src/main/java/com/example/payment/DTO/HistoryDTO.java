package com.example.payment.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record HistoryDTO(
        Long id,
        Long userId,
        BigDecimal amount,
        String type,
        String description,
        LocalDateTime timestamp) {}