package com.vbartere.Shared.Kafka;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CartEvent {
    private Long userId;
    private Long advertisementId;

    @JsonCreator
    public CartEvent(@JsonProperty("userId") Long userId, @JsonProperty("advertisementId") Long advertisementId) {
        this.userId = userId;
        this.advertisementId = advertisementId;
    }

    // Геттеры и сеттеры
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(Long advertisementId) {
        this.advertisementId = advertisementId;
    }
}
