package com.example.Referral.kafka.DTO;

import java.math.BigDecimal;

public class PaymentEventDTO {
    Long userId;
    private BigDecimal commission;

    public PaymentEventDTO(Long userId, BigDecimal comission) {
        this.userId = userId;
        this.commission = comission;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    @Override
    public String toString() {
        return "PaymentEventDTO{" +
                "userId=" + userId +
                ", commission=" + commission +
                '}';
    }
}
