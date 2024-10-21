package com.vbartere.Advertisement.Controller;

import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Repository.ImageRepository;
import com.vbartere.Advertisement.Service.ImageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    public ImageController(ImageRepository imageRepository, ImageService imageService) {
        this.imageRepository = imageRepository;
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    private ResponseEntity<?> getImageById(@PathVariable("id") Long id) {
        Image image = imageRepository.findById(id).orElse(null);
        assert image != null;
        return ResponseEntity.ok()
                .header("fileName", image.getOriginalFileName())
                .contentType(MediaType.valueOf(image.getContentType()))
                .contentLength(image.getSize())
                .body(new InputStreamResource(new ByteArrayInputStream(image.getBytes())));
    }

    @PostMapping("/upload")
    public ResponseEntity<List<Image>> uploadImage(@RequestPart("files") List<MultipartFile> file) {
        try {
            List<Image> createdAd = imageService.createImages(file);
            return ResponseEntity.ok(createdAd);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
