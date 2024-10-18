package com.vbartere.userservice.controller;

import com.vbartere.userservice.model.User;
import com.vbartere.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Регистрация пользователя
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody Map<String, String> userDto) {
        String phoneNumber = userDto.get("phoneNumber");
        String password = userDto.get("password");
        User registeredUser = userService.registerUser(phoneNumber, password);
        return ResponseEntity.ok(registeredUser);
    }

    // Логин пользователя
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody Map<String, String> userDto) {
        String phoneNumber = userDto.get("phoneNumber");
        String password = userDto.get("password");
        User loggedInUser = userService.loginUser(phoneNumber, password);
        return ResponseEntity.ok(loggedInUser);
    }

    // Обновление данных пользователя
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUserDetails(@PathVariable Long userId, @RequestBody Map<String, String> userDetails) {
        String name = userDetails.get("name");
        String surname = userDetails.get("surname");
        User updatedUser = userService.updateUserDetails(userId, name, surname);
        return ResponseEntity.ok(updatedUser);
    }

    // Проверка, зарегистрирован ли номер телефона
    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhoneNumber(@RequestParam String phoneNumber) {
        boolean isRegistered = userService.isPhoneNumberRegistered(phoneNumber);
        return ResponseEntity.ok(isRegistered);
    }
}




