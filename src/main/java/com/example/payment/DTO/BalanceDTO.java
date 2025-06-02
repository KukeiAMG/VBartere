package com.example.payment.DTO;

import java.math.BigDecimal;

public record BalanceDTO(
        Long userId,
    BigDecimal balance,
    String currency
) {} 