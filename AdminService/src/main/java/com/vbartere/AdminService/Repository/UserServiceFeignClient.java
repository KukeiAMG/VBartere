package com.vbartere.AdminService.Repository;

import com.vbartere.AdminService.Model.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", url = "${services.UserService.url}")
public interface UserServiceFeignClient {
    @GetMapping("/users")
    List<UserDTO> getAllUsers();

    @PutMapping("/users/{userId}/ban")
    void updateBanStatus(@PathVariable("userId") Long userId, @RequestParam("isBanned") boolean isBanned);
}
