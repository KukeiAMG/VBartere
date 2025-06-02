package com.example.payment.kafka.DTO;

import com.example.payment.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NotificationPurchaseDTO {
    private Long buyersId;
    private TransactionType transactionType;
    private BigDecimal purchasePrice;
    private BigDecimal buyersAccountBalance;
     
    private LocalDateTime purchaseTime;


}
