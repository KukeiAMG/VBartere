package com.vbartere.Advertisement.Mapper;

import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import com.vbartere.Shared.Kafka.Enum.AdvertisementEventType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AdvertisementMapper {

    public AdvertisementDTO advertisementToDTO(Advertisement advertisement) {
        AdvertisementDTO dto = new AdvertisementDTO();
        dto.setId(advertisement.getId());
        dto.setTitle(advertisement.getTitle());
        dto.setDescription(advertisement.getDescription());
        dto.setOwnerId(advertisement.getOwnerId());
        dto.setBuyersId(advertisement.getBuyersId());
        dto.setStatus(advertisement.getStatus());

        if (advertisement.getStatus() != null) {
            dto.setStatus(advertisement.getStatus());
        } else {
            dto.setStatus(false); // Значение по умолчанию, если status = null
        }

        if (advertisement.getSubcategory() != null) {
            dto.setSubCategoryId(advertisement.getSubcategory().getId());
        }

        if (advertisement.getImageList() != null && !advertisement.getImageList().isEmpty()) {
            List<Long> imageIds = new ArrayList<>();
            for (Image image : advertisement.getImageList()) {
                imageIds.add(image.getId());
            }
            dto.setImagesId(imageIds);
        } else {
            dto.setImagesId(Collections.emptyList());
        }

        return dto;
    }

    public AdminAdvertisementDTO toAdminDto(Advertisement ad, AdvertisementEventType eventType) {
        AdminAdvertisementDTO dto = new AdminAdvertisementDTO();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setStatus(ad.getStatus());
        dto.setOwnerId(ad.getOwnerId());
        dto.setBuyersId(ad.getBuyersId());
        dto.setSubcategoryId(ad.getSubcategory().getId());
        dto.setSubcategoryTitle(ad.getSubcategory().getName());
        dto.setEventType(eventType);
        return dto;
    }
}
