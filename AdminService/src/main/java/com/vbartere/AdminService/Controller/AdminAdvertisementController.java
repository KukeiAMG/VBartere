package com.vbartere.AdminService.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vbartere.AdminService.Service.AdminAdvertisementService;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/admin/advertisements/")
public class AdminAdvertisementController {

    private final AdminAdvertisementService adminAdvertisementService;

    public AdminAdvertisementController(AdminAdvertisementService adminAdvertisementService) {
        this.adminAdvertisementService = adminAdvertisementService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdvertisement(@PathVariable Long id) {
        try {
            AdminAdvertisementDTO adminAdvertisementDTO = adminAdvertisementService.getById(id);
            return ResponseEntity.ok().body(adminAdvertisementDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<AdminAdvertisementDTO>> getAllAdvertisements() {
        return ResponseEntity.ok().body(adminAdvertisementService.getAll());
    }

    @PostMapping("/{id}/ban")
    public ResponseEntity<?> banAdvertisement(@PathVariable Long id,
                                              @RequestBody String description) {
        try {
            adminAdvertisementService.banAdvertisement(id, description);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
