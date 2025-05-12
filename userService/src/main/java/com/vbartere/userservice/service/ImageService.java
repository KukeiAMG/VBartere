package com.vbartere.userservice.service;

import com.vbartere.userservice.model.Image;
import com.vbartere.userservice.repository.ImageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {
    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Image getImageById(Long id) {
        return imageRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Изображение не найдено"));
    }
}
