package com.vbartere.userservice.model.Embeddable;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class CartItem {
    private Long advertisementId;
    private BigDecimal price;
    private Boolean selected;

    public CartItem() {}

    public CartItem(Long advertisementId, BigDecimal price, Boolean selected) {
        this.advertisementId = advertisementId;
        this.price = price;
        this.selected = selected;
    }

    public Long getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(Long advertisementId) {
        this.advertisementId = advertisementId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "advertisementId=" + advertisementId +
                ", price=" + price +
                ", selected=" + selected +
                '}';
    }
}