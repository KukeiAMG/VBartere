package com.vbartere.userservice.service;

import com.vbartere.userservice.model.Role;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.RoleRepository;
import com.vbartere.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtService jwtService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }

    public String loginUser(String phoneNumber, String password) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("wrong phone number or password"));

        // Генерация JWT токена
        return jwtService.generateToken(user.getPhoneNumber());
    }

    public User registerUser(String phoneNumber, String password) {
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("user already exists");
        }
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("role not found");
        }

        if (user.getRoles().contains(role)) {
            throw new IllegalArgumentException("user already has this role");
        }

        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public User updateUserDetails(Long userId, String name, String surname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        user.setName(name);
        user.setSurname(surname);
        return userRepository.save(user);
    }

    public Long getUserIdByPhoneNumber(String token) {
        User user = userRepository.findByPhoneNumber(jwtService.extractPhoneNumber(token))
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return user.getId();
    }

    public boolean isPhoneNumberRegistered(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }
}



