package com.vbartere.AdminService.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vbartere.AdminService.Model.AdminUser;
import com.vbartere.AdminService.Service.AdminUserService;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/admin/users/")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {
        try {
            AdminUserDTO adminUserDTO = adminUserService.getById(id);
            return ResponseEntity.ok().body(adminUserDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok().body(adminUserService.getAll());
    }

    @PostMapping("/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable("id") Long id,
                                     @RequestBody String description) {
        try {
            adminUserService.banUser(id, description);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
