package com.vbartere.userservice.controller;

import com.vbartere.userservice.service.ImageService;
import com.vbartere.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserImageController {

    private final UserService userService;
    private final ImageService imageService;

    public UserImageController(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @GetMapping("/{id}/image/get")
    public ResponseEntity<?> getImageById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(imageService.getImageById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadUserImage(@PathVariable("id") Long id,
                                             @RequestParam("file") MultipartFile file) {
        try {
            userService.uploadUserImage(id, file);
            return ResponseEntity.ok().body(Map.of("message", "Изображение успешно загружено"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при чтении файла"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<?> deleteUserImage(@PathVariable("id") Long id) {
        try {
            userService.removeUserImage(id);
            return ResponseEntity.ok().body(Map.of("message", "Изображение удалено"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
