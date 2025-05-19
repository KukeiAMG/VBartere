package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Repository.ImageRepository;
import com.vbartere.Shared.Kafka.DTO.Advertisement.ImageDTO;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final AdvertisementRepository advertisementRepository;

    @Value("${image.base-url}")
    private String IMAGE_URL;

    @Value("${image.base-dir}")
    private String IMAGE_DIR;

    public ImageService(ImageRepository imageRepository, AdvertisementRepository advertisementRepository) {
        this.imageRepository = imageRepository;
        this.advertisementRepository = advertisementRepository;
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {

        String uploadDir = IMAGE_DIR;

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path filePath = Paths.get(uploadDir, fileName);
        Files.createDirectories(filePath.getParent()); // создать папку если не существует
        file.transferTo(filePath.toFile()); // сохранить файл

        return filePath.toString();
    }

    private Image toImageEntity(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getOriginalFilename());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());

        String filePath = saveFileToDisk(file);
        image.setFilePath(filePath);

        return image;
    }

    @Transactional(readOnly = true)
    public Image getImageById(Long id) {
        return imageRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Изображение не найдено"));
    }

    @Transactional(readOnly = true)
    public List<ImageDTO> getImageMetadataByAdvertisementId(Long advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

        List<Image> images = advertisement.getImageList();
        List<ImageDTO> dtos = new ArrayList<>();

        for (Image image : images) {
            ImageDTO dto = new ImageDTO();
            dto.setId(image.getId());
            dto.setName(image.getName());
            dto.setOriginalFileName(image.getOriginalFileName());
            dto.setContentType(image.getContentType());
            dto.setSize(image.getSize());
            dto.setPreviewImage(image.isPreviewImage());

            // Формируем URL для доступа к файлу через контроллер (например, /images/{id})
            dto.setUrl("/images/" + image.getId());

            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    public Image createImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String uploadDir = IMAGE_DIR;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, uniqueFileName);
        Files.write(path, file.getBytes());

        Image image = new Image();
        image.setName(file.getOriginalFilename());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setFilePath(path.toString());

        return imageRepository.save(image);
    }

    @Transactional
    public List<ImageDTO> createImages(List<MultipartFile> files) throws IOException {
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : files) {
            Image image = toImageEntity(file); // сохранить файл на диск и получить сущность с путём
            images.add(image);
        }
        List<Image> savedImages = imageRepository.saveAll(images);

        List<ImageDTO> result = new ArrayList<>();
        for (Image img : savedImages) {
            ImageDTO dto = new ImageDTO();
            dto.setId(img.getId());
            dto.setName(img.getName());
            dto.setOriginalFileName(img.getOriginalFileName());
            dto.setContentType(img.getContentType());
            dto.setSize(img.getSize());
            dto.setPreviewImage(img.isPreviewImage());
            dto.setUrl("/images/" + img.getId());  // формируем URL для отдачи клиенту
            result.add(dto);
        }

        return result;
    }

    @Transactional
    public void deleteImageById(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Изображение не найдено"));

        if (image.getFilePath() != null) {
            Path path = Paths.get(image.getFilePath());
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                System.err.println("Не удалось удалить файл: " + path + ", причина: " + e.getMessage());
            }
        }

        imageRepository.delete(image);
    }
}
