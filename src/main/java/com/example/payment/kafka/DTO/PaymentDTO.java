package com.example.payment.kafka.DTO;

import java.math.BigDecimal;
import java.util.Map;

public class PaymentDTO {
    private Long buyersId;
    private Map<Long, BigDecimal> advertisementsWithPrice;

    public PaymentDTO() {
    }

    public PaymentDTO(Long buyersId, Map<Long, BigDecimal> advertisementsWithPrice) {
        this.buyersId = buyersId;
        this.advertisementsWithPrice = advertisementsWithPrice;
    }

    public Long getBuyersId() {
        return buyersId;
    }

    public void setBuyersId(Long buyersId) {
        this.buyersId = buyersId;
    }

    public Map<Long, BigDecimal> getAdvertisementsWithPrice() {
        return advertisementsWithPrice;
    }

    public void setAdvertisementsWithPrice(Map<Long, BigDecimal> advertisementsWithPrice) {
        this.advertisementsWithPrice = advertisementsWithPrice;
    }

    @Override
    public String toString() {
        return "PaymentDTO{" +
                "buyersId=" + buyersId +
                ", advertisementsWithPriсe=" + advertisementsWithPrice +
                '}';
    }
}
