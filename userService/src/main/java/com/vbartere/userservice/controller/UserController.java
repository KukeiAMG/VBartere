package com.vbartere.userservice.controller;

import com.vbartere.userservice.model.User;
import com.vbartere.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        String email = userDto.get("email");
        String password = userDto.get("password");
        User registeredUser = userService.registerUser(email, password);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody Map<String, String> userDto) {
        String email = userDto.get("email");
        String password = userDto.get("password");
        User loggedInUser = userService.loginUser(email, password);
        return ResponseEntity.ok(loggedInUser);
    }
}




