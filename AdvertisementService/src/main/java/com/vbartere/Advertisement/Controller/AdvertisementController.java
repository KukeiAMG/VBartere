package com.vbartere.Advertisement.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Security.JwtService;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Advertisement.exceptions.InvalidTokenException;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import com.vbartere.Shared.Kafka.Enum.AdvertisementEventType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
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
    private final JwtService jwtService;

    public AdvertisementController(AdvertisementService advertisementService, JwtService jwtService) {
        this.advertisementService = advertisementService;
        this.jwtService = jwtService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AdvertisementDTO>> getAllAdvertisements() {
        List<AdvertisementDTO> advertisementList = advertisementService.getAllAdvertisements();
        return ResponseEntity.ok(advertisementList);
    }

    @GetMapping("/{id}")
    public AdvertisementDTO getAdvertisement(@PathVariable Long id) throws ExecutionException, JsonProcessingException, InterruptedException {
        return advertisementService.getAdvertisementById(id);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createAdvertisement( @RequestPart("advertisement") AdvertisementDTO advertisementDTO,
                                                  @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InvalidTokenException("Невалидный формат токена");
            }

            String token = authHeader.substring(7);
            Long userId = jwtService.getUserId(token);

            AdvertisementDTO createdAd = advertisementService.createAdvertisement(advertisementDTO, files, userId);
            return ResponseEntity.ok(createdAd);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEPRECATED
     * <p>Этот контроллер используется исключительно для сдачи
     * <p> Используй {@link AdvertisementController#updateAdvertisementFields} для обновления полей объявления
     * <p> Используй {@link AdvertisementController#updateAdvertisementImages} для обновления фотографий объявления
     */
    @PutMapping(value = "/{id}/update", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateAdvertisement(@PathVariable Long id,
                                                 @RequestPart("advertisement") AdvertisementDTO advertisementDTO,
                                                 @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            if (!hasAccessToAdvertisement(authHeader, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Нет доступа"));
            }

            AdvertisementDTO updatedAd = advertisementService.updateAdvertisementById(id, advertisementDTO, files);
            return ResponseEntity.ok(updatedAd);
        } catch (EntityNotFoundException | IOException | ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/update-fields")
    public ResponseEntity<?> updateAdvertisementFields(@PathVariable Long id,
                                                       @RequestBody AdvertisementDTO advertisementDTO,
                                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            if (!hasAccessToAdvertisement(authHeader, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Нет доступа"));
            }

            AdvertisementDTO updatedAd = advertisementService.updateAdvertisementFields(id, advertisementDTO);
            return ResponseEntity.ok(updatedAd);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}/update-images", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateAdvertisementImages(@PathVariable Long id,
                                                       @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            if (!hasAccessToAdvertisement(authHeader, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Нет доступа"));
            }

            AdvertisementDTO updatedAd = advertisementService.updateAdvertisementImages(id, files);
            return ResponseEntity.ok(updatedAd);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
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

    private boolean hasAccessToAdvertisement(String authHeader, Long advertisementId) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Невалидный формат токена");
        }

        String token = authHeader.substring(7);
        Long userId = jwtService.getUserId(token);

        List<String> roles = jwtService.getAuthorities(token)
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Advertisement advertisement = advertisementService.getAdvertisementEntity(advertisementId);

        boolean isOwner = advertisement.getOwnerId().equals(userId);
        boolean isAdmin = roles.contains("ROLE_ADMIN");

        return isOwner || isAdmin;
    }
}
