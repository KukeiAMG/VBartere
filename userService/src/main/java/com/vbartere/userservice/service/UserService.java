package com.vbartere.userservice.service;

import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public String loginUser(String phoneNumber, String password) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("Неверный номер телефона или пароль"));

        // Генерация JWT токена
        return jwtService.generateToken(user.getPhoneNumber());
    }

    public User registerUser(String phoneNumber, String password) {
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким номером телефона уже существует");
        }
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPassword(password);
        return userRepository.save(user);
    }

    public User updateUserDetails(Long userId, String name, String surname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        user.setName(name);
        user.setSurname(surname);
        return userRepository.save(user);
    }


    public boolean isPhoneNumberRegistered(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }
}



