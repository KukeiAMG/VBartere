package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Repository.ImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
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

    private Image toImageEntity(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());
        return image;
    }

    public Image getImageById(Long id) {
        return imageRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Изображение не найдено")
        );
    }

    @Transactional
    public Image createImage(MultipartFile file) throws IOException {
        Image image = toImageEntity(file);
        imageRepository.save(image);
        return image;
    }

    @Transactional
    public List<Image> createImages(List<MultipartFile> file) throws IOException {
        List<Image> images = new ArrayList<>();
        for(MultipartFile file1 : file) {
            Image image = toImageEntity(file1);
            images.add(image);
            imageRepository.save(image);
        }
        return images;
    }

    @Transactional
    public void deleteImageById(Long id) {
        imageRepository.deleteById(id);
    }
}
