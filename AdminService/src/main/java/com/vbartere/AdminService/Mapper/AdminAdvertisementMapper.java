package com.vbartere.AdminService.Mapper;

import com.vbartere.AdminService.Model.AdminAdvertisement;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import org.springframework.stereotype.Component;

@Component
public class AdminAdvertisementMapper {
    public AdminAdvertisementDTO toDTO(AdminAdvertisement entity) {
        if (entity == null) return null;

        AdminAdvertisementDTO dto = new AdminAdvertisementDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setSubcategoryId(entity.getSubcategoryId());
        dto.setSubcategoryTitle(entity.getSubcategoryTitle());
        dto.setImageUrls(entity.getImageUrls());
        dto.setOwnerId(entity.getOwnerId());
        dto.setOwnerUsername(entity.getOwnerUsername());
        dto.setBuyersId(entity.getBuyersId());
        dto.setBuyerUsername(entity.getBuyerUsername());
        dto.setStatus(entity.getStatus());
        dto.setBanStatus(entity.getBanStatus());

        return dto;
    }
}
