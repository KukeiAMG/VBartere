package com.vbartere.Advertisement.Controller;

import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Repository.ImageRepository;
import com.vbartere.Advertisement.Service.ImageService;
import com.vbartere.Shared.Kafka.DTO.Advertisement.ImageDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    private ResponseEntity<?> getImageById(@PathVariable("id") Long id) {
        try {
            Image image = imageService.getImageById(id);
            Path path = Paths.get(image.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Файл не найден или недоступен"));
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getOriginalFileName() + "\"")
                    .body(resource);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка обработки пути к файлу"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Внутренняя ошибка сервера"));
        }
    }

    @GetMapping("/advertisements/{id}/images")
    public ResponseEntity<List<ImageDTO>> getImagesMetadata(@PathVariable Long id) {
        List<ImageDTO> images = imageService.getImageMetadataByAdvertisementId(id);
        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<?> deleteImageById(@PathVariable("id") Long id) {
        try {
            imageService.deleteImageById(id);
            return ResponseEntity.ok().body("Изображения успешно удалено");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
