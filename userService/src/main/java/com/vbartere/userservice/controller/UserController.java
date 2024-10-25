package com.vbartere.userservice.controller;

import com.vbartere.userservice.model.User;
import com.vbartere.userservice.service.JwtService;
import com.vbartere.userservice.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    private String USER_TOKEN;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody Map<String, String> userDto) {
        String phoneNumber = userDto.get("phoneNumber");
        String password = userDto.get("password");
        User registeredUser = userService.registerUser(phoneNumber, password);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> userDto) {
        String phoneNumber = userDto.get("phoneNumber");
        String password = userDto.get("password");

        USER_TOKEN = userService.loginUser(phoneNumber, password);

        Map<String, String> response = new HashMap<>();
        response.put("token", USER_TOKEN);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUserDetails(@PathVariable Long userId, @RequestBody Map<String, String> userDetails) {
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
    public ResponseEntity<Boolean> validateToken(@RequestParam(value = "token") String token, @RequestParam(value = "phoneNumber") String phoneNumber) {
        try {
            jwtService.validateToken(token, phoneNumber);
            return ResponseEntity.ok(true); // Если токен валиден
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return ResponseEntity.ok(false); // Если токен не валиден
        }
    }

    @GetMapping("/getId")
    public ResponseEntity<Long> getUserId(@RequestParam(value = "token") String token) {
        try {
            Long userId = jwtService.extractUserId(token);
            System.out.println(userId);
            return ResponseEntity.ok(userId); // Если токен валиден
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return ResponseEntity.ok(null); // Если токен не валиден
        }
    }
}




