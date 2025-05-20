package com.vbartere.userservice.controller;

import com.vbartere.Shared.Kafka.DTO.Advertisement.ImageDTO;
import com.vbartere.userservice.exceptions.InvalidTokenException;
import com.vbartere.userservice.model.Image;
import com.vbartere.userservice.service.ImageService;
import com.vbartere.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "http://localhost:4200")
public class UserImageController {

    private final ImageService imageService;
    private final UserService userService;

    public UserImageController(ImageService imageService, UserService userService) {
        this.imageService = imageService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    private ResponseEntity<?> getImageById(@PathVariable("id") Long id) {
        try {
            Image image = imageService.getImageById(id);
            Path path = Paths.get(image.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.getContentType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getOriginalFileName() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Файл не найден или недоступен");
            }
        } catch (MalformedURLException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/metadata")
    public ResponseEntity<?> getImagesMetadata(@PathVariable Long userId) {
        try {
            ImageDTO image = imageService.getImageMetadataByUserId(userId);
            return ResponseEntity.ok(image);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestPart("file") MultipartFile file,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InvalidTokenException("Невалидный формат токена");
            }

            String token = authHeader.substring(7);
            Long userId = userService.getUserIdByToken(token);

            ImageDTO image = imageService.createImage(file, userId);

            return ResponseEntity.ok(image);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-my-profile-photo")
    public ResponseEntity<?> removeUserImage(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InvalidTokenException("Невалидный формат токена");
            }

            String token = authHeader.substring(7);
            Long userId = userService.getUserIdByToken(token);

            userService.removeUserImage(userId);
            return ResponseEntity.ok(Map.of("message", "Изображение успешно удалено"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Пользователь не найден"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Произошла ошибка при удалении изображения"));
        }
    }
}
