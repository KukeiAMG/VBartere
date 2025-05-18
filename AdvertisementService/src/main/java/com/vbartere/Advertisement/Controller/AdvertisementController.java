package com.vbartere.Advertisement.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Advertisement>> getAllAdvertisements() {
        List<Advertisement> advertisementList = advertisementService.getAllAdvertisements();
        return ResponseEntity.ok(advertisementList);
    }

    @GetMapping("/{id}")
    public AdvertisementDTO getAdvertisement(@PathVariable Long id) throws ExecutionException, JsonProcessingException, InterruptedException {
        return advertisementService.getAdvertisementById(id);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createAdvertisement( @RequestPart("advertisement") AdvertisementDTO advertisementDTO,
                                                  @RequestPart("files") List<MultipartFile> files,
                                                  @RequestHeader("user-ID") Long userId) {
        try {
            AdvertisementDTO createdAd = advertisementService.createAdvertisement(advertisementDTO, files, userId);
            return ResponseEntity.ok(createdAd);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}/update", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateAdvertisement(@PathVariable Long id,
                                                 @RequestPart("advertisement") AdvertisementDTO advertisementDTO,
                                                 @RequestPart("files") List<MultipartFile> files) {
        try {
            AdvertisementDTO updatedAd = advertisementService.updateAdvertisementById(id, advertisementDTO, files);
            return ResponseEntity.ok(updatedAd);
        } catch (EntityNotFoundException | IOException | ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteAdvertisement(@PathVariable("id") Long id) {
        try {
            advertisementService.deleteAdvertisementById(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
