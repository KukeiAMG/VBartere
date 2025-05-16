package com.vbartere.AdminService.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class AdminUserController {

//    private final UserService userService;
//
//    public AdminUserController(UserService userService) {
//        this.userService = userService;
//    }
//
//    @PostMapping("/sync-users")
//    public List<User> syncUsers() {
//        return userService.fetchAndSaveUsersFromUserService();
//    }
//
//    @GetMapping("/all")
//    public List<User> getAllUsersFromDB() {
//        return userService.getAllUsersFromDB();
//    }
//
//    @PutMapping("/users/{userId}/ban")
//    public ResponseEntity<String> banUser(@PathVariable("userId") Long userId, @RequestParam("isBanned") boolean isBanned) {
//        userService.banUser(userId, isBanned);
//        return ResponseEntity.ok("Пользователь " + userId + " " + (isBanned ? "забанен" : "разбанен"));
//    }
}
