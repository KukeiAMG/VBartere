package com.vbartere.Advertisement.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdvertisementDTO {
    private String title;
    private String description;
    private Long subCategoryId; // Используем ID для связи с SubCategory
    private List<Long> imageIds; // Используем список ID для связи с Image
    private Long userId;
    private boolean status;

    public AdvertisementDTO() {}

    public AdvertisementDTO(String title, String description, Long subCategoryId, List<Long> imageIds, Long userId, boolean status) {
        this.title = title;
        this.description = description;
        this.subCategoryId = subCategoryId;
        this.imageIds = imageIds;
        this.userId = userId;
        this.status = status;
    }
}
