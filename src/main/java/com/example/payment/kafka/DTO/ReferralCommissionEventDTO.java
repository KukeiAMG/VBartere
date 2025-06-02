package com.example.payment.kafka.DTO;

import java.math.BigDecimal;

public class ReferralCommissionEventDTO {
    private Long userId;
    private BigDecimal commission;

    public ReferralCommissionEventDTO(Long userId, BigDecimal commission) {
        this.userId = userId;
        this.commission = commission;
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
        return "ReferralCommissionEventDTO{" +
                "userId=" + userId +
                ", commission=" + commission +
                '}';
    }
}
