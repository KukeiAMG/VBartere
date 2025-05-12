package com.vbartere.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vbartere.userservice.DTO.RegisterUserRequest;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.service.JwtService;
import com.vbartere.userservice.service.RefreshTokenService;
import com.vbartere.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    private String USER_TOKEN;

    @GetMapping("/{id}/get")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(userService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);

            Long userId = userService.getUserIdByPhoneNumber(token);

            User user = userService.getById(userId);

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("id", String.valueOf(user.getId()));
            userInfo.put("phoneNumber", user.getPhoneNumber());
            userInfo.put("email", user.getEmail());
            userInfo.put("name", user.getName());

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невалидный или устаревший токен");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserRequest userDto) throws JsonProcessingException {
        try {
            String phoneNumber = userDto.getPhoneNumber();
            String email = userDto.getEmail();
            String password = userDto.getPassword();
            String invitedByCode = userDto.getInvitedByCode();

            User registeredUser = userService.registerUser(phoneNumber, password, email, invitedByCode);

            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> userDto) throws JsonProcessingException {
        try {
            String phoneNumber = userDto.get("phoneNumber");
            String password = userDto.get("password");

            // Используем обновлённый метод loginUser для аутентификации и генерации токенов
            Map<String, String> tokens = userService.loginUser(phoneNumber, password);

            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam Map<String, String> body) {
        try {
            String refreshToken = body.get("refreshToken");
            String newAccessToken = refreshTokenService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(Collections.singletonMap("accessToken", newAccessToken));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невалидный токен");
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUserDetails(@PathVariable Long userId, @RequestBody Map<String, String> userDetails) throws JsonProcessingException {
        String name = userDetails.get("name");
        String surname = userDetails.get("surname");
        User updatedUser = userService.updateUserDetails(userId, name, surname);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhoneNumber(@RequestParam String phoneNumber) {
        boolean isRegistered = userService.isPhoneNumberRegistered(phoneNumber);
        return ResponseEntity.ok(isRegistered);
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<User> assignRole(@PathVariable Long userId, @RequestParam String roleName) {
        User user = userService.assignRoleToUser(userId, roleName);
        return ResponseEntity.ok(user);
    }

    // Ключ для подписи токена из JwtService
    private static final String SECRET_KEY = "5FZsRG9Q2f9UvdxeUR4iU5FV9nFg1Hn9zPb49M8uV7o=";
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestBody Map<String, String> userDetails) {
        try {
            String token = userDetails.get("token");
            String phoneNumber = userDetails.get("phoneNumber");
            jwtService.validateToken(token, phoneNumber);
            return ResponseEntity.ok(true); // Если токен валиден
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return ResponseEntity.ok(false); // Если токен не валиден
        }
    }

    @GetMapping("/getCurrentUserId")
    public ResponseEntity<?> getUserId(@RequestParam(value = "token") String token) {
        try {
            Long userId = userService.getUserIdByPhoneNumber(token);
            return ResponseEntity.ok(userId); // Если токен валиден
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}




