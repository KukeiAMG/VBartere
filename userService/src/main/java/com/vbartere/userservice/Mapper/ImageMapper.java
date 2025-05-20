package com.vbartere.userservice.Mapper;

import com.vbartere.Shared.Kafka.DTO.Advertisement.ImageDTO;
import com.vbartere.userservice.model.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {
    public ImageDTO toDTO(Image image) {
        if (image == null) {
            return null;
        }

        ImageDTO dto = new ImageDTO();
        dto.setId(image.getId());
        dto.setName(image.getName());
        dto.setOriginalFileName(image.getOriginalFileName());
        dto.setContentType(image.getContentType());
        dto.setSize(image.getSize());
        dto.setPreviewImage(image.isPreviewImage());
        dto.setUrl("/images/" + image.getId()); // или другой базовый путь

        return dto;
    }
}
