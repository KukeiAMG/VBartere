package com.vbartere.Advertisement.DTO;

import com.vbartere.Advertisement.Model.Advertisement;

public class AdvertisementMapper {
    public static Advertisement toEntity(AdvertisementDTO dto, Advertisement existingEntity) {
        existingEntity.setBuyersId(dto.getBuyersId());
        existingEntity.setStatus(dto.isStatus());
        return existingEntity;
    }
}
