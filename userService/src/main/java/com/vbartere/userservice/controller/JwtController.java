package com.vbartere.userservice.controller;

import com.vbartere.userservice.exceptions.InvalidTokenException;
import com.vbartere.userservice.service.JwtService;
import com.vbartere.userservice.service.UserService;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jwt/")
public class JwtController {

    private final UserService userService;
    private final JwtService jwtService;

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
}
