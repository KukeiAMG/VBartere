package com.vbartere.Advertisement.Controller;

import com.vbartere.Advertisement.DTO.AdvertisementDTO;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Service.AdvertisementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advertisements")
public class RestAdvController {
    private final AdvertisementService advertisementService;

    public RestAdvController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @PostMapping
    public ResponseEntity<Advertisement> createAdvertisement(@RequestBody AdvertisementDTO advertisementDTO) {
        try {
            Advertisement createdAd = advertisementService.createAdvertisement(advertisementDTO);
            return ResponseEntity.ok(createdAd);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public Advertisement getAdvertisement(@PathVariable Long id) {
        return advertisementService.getAdvertisementById(id);
    }
}
