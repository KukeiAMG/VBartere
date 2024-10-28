package com.vbartere.Advertisement.Controller;

import com.vbartere.Advertisement.DTO.AdvertisementDTO;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Service.AdvertisementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public Advertisement getAdvertisement(@PathVariable Long id) {
        return advertisementService.getAdvertisementById(id);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<Advertisement> createAdvertisement(@RequestPart("advertisement") AdvertisementDTO advertisementDTO,
                                                             @RequestPart("files") List<MultipartFile> files,
                                                             @RequestHeader("user-ID") Long userId) {
        try {
            Advertisement createdAd = advertisementService.createAdvertisement(advertisementDTO, files, userId);
            return new ResponseEntity<>(createdAd, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping(value = "/{id}/update", consumes = {"multipart/form-data"})
    public ResponseEntity<Advertisement> updateAdvertisement(@PathVariable Long id,
                                                             @RequestPart("advertisement") AdvertisementDTO advertisementDTO,
                                                             @RequestPart("files") List<MultipartFile> files) {
        try {
            Advertisement updatedAd = advertisementService.updateAdvertisementById(id, advertisementDTO, files);
            return ResponseEntity.ok(updatedAd);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Advertisement> deleteAdvertisement(@PathVariable("id") Long id) {
        try {
            advertisementService.deleteAdvertisementById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
