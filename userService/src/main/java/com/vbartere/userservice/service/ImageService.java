package com.vbartere.userservice.service;

import com.vbartere.Shared.Kafka.DTO.Advertisement.ImageDTO;
import com.vbartere.userservice.Mapper.ImageMapper;
import com.vbartere.userservice.model.Image;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.ImageRepository;
import com.vbartere.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ImageMapper imageMapper;

    @Value("${image.base-url}")
    private String IMAGE_URL;

    @Value("${image.base-dir}")
    private String IMAGE_DIR;

    public ImageService(ImageRepository imageRepository, UserRepository userRepository, ImageMapper imageMapper) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.imageMapper = imageMapper;
    }

    @Transactional(readOnly = true)
    public Image getImageById(Long id) {
        return imageRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Изображение не найдено"));
    }

    @Transactional(readOnly = true)
    public ImageDTO getImageMetadataByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("пользователь не найден в БД")
        );

        Image image = user.getImage();

        if (image == null) {
            throw new EntityNotFoundException("Изображение у пользователя не найдено или отсутствует");
        }

        ImageDTO dto = new ImageDTO();
        dto.setId(image.getId());
        dto.setName(image.getName());
        dto.setOriginalFileName(image.getOriginalFileName());
        dto.setContentType(image.getContentType());
        dto.setSize(image.getSize());
        dto.setPreviewImage(image.isPreviewImage());

        dto.setUrl("/images/" + image.getId());

        return dto;
    }

    @Transactional
    public ImageDTO createImage(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Image oldImage = user.getImage();
        if (oldImage != null) {
            try {
                Files.deleteIfExists(Paths.get(oldImage.getFilePath()));
            } catch (IOException e) {
                System.err.println("Ошибка при удалении старого изображения: " + e.getMessage());
            }
        }

        String uploadDir = IMAGE_DIR;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, uniqueFileName);
        Files.write(path, file.getBytes());

        Image newImage = new Image();
        newImage.setName(file.getOriginalFilename());
        newImage.setOriginalFileName(file.getOriginalFilename());
        newImage.setContentType(file.getContentType());
        newImage.setSize(file.getSize());
        newImage.setFilePath(path.toString());
        newImage.setUser(user);
        newImage.setPreviewImage(true);
        imageRepository.save(newImage);

        user.setImage(newImage);

        userRepository.save(user);

        ImageDTO imageDTO = imageMapper.toDTO(newImage);
        imageDTO.setPreviewImage(newImage.isPreviewImage());
        imageDTO.setUrl("/images/" + newImage.getId());

        return imageDTO;
    }
}
