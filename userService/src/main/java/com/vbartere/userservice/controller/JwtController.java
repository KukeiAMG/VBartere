package com.vbartere.userservice.controller;

import com.vbartere.Shared.Kafka.DTO.Gateway.UserInfoDTO;
import com.vbartere.userservice.exceptions.InvalidTokenException;
import com.vbartere.userservice.service.JwtService;
import com.vbartere.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jwt/")
@EnableMethodSecurity(securedEnabled = true)
public class JwtController {

    private final UserService userService;
    private final JwtService jwtService;
    final String expectedSecret = "my-super-secret";

    public JwtController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestBody Map<String, String> userDetails) {
        try {
            String token = userDetails.get("token");
            String phoneNumber = userDetails.get("phoneNumber");

            if (token == null || phoneNumber == null) {
                return ResponseEntity.badRequest().body(false);
            }

            jwtService.validateToken(token, phoneNumber);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/getCurrentUserId")
    public ResponseEntity<Long> getCurrentUserId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InvalidTokenException("Невалидный формат токена");
            }

            String token = authHeader.substring(7);
            Long userId = userService.getUserIdByToken(token);

            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/getCurrentUserRoles")
    public ResponseEntity<?> getCurrentUserRoles(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gatewaySecret
    ) {
        if (!expectedSecret.equals(gatewaySecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        List<String> roles = jwtService.extractRoles(token);

        return ResponseEntity.ok(roles);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/currentUserInfo")
    public ResponseEntity<UserInfoDTO> getCurrentUserInfo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader(value = "X-Gateway-Secret") String gatewaySecret
    ) {
        if (!expectedSecret.equals(gatewaySecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String token = authHeader.substring(7);
            String phoneNumber = jwtService.extractPhoneNumber(token);

            UserInfoDTO userInfo = userService.getUserByPhoneNumberWithRoles(phoneNumber);

            return ResponseEntity.ok(userInfo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
