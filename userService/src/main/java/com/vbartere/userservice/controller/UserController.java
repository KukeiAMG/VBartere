package com.vbartere.userservice.controller;

import com.vbartere.userservice.model.User;
import com.vbartere.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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

        String token = userService.loginUser(phoneNumber, password);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
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
}




