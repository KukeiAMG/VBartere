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
    private Long ownerId;
    private Long buyersId;
    private List<Long> imagesId;
    private boolean status;

    public AdvertisementDTO() {}

    public AdvertisementDTO(String title, String description, Long subCategoryId, Long ownerId, Long buyersId, List<Long> imagesId, boolean status) {
        this.title = title;
        this.description = description;
        this.subCategoryId = subCategoryId;
        this.ownerId = ownerId;
        this.buyersId = buyersId;
        this.imagesId = imagesId;
        this.status = status;
    }

    @Override
    public String toString() {
        return "AdvertisementDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", subCategoryId=" + subCategoryId +
                ", ownerId=" + ownerId +
                ", buyersId=" + buyersId +
                ", imagesId=" + imagesId +
                ", status=" + status +
                '}';
    }
}
