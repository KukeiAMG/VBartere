package com.vbartere.userservice.service;

import com.vbartere.userservice.model.RefreshToken;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.RefreshTokenRepository;
import com.vbartere.userservice.repository.UserRepository;
import org.apache.kafka.common.errors.DelegationTokenExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RefreshTokenService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JwtService jwtService, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String refreshAccessToken(String refreshToken) {
        String phoneNumber = jwtService.extractPhoneNumber(refreshToken);
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден в БД"));

        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);

        RefreshToken matchedToken = tokens.stream()
                .filter(stored -> passwordEncoder.matches(refreshToken, stored.getToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Невалидный токен"));

        if (matchedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Обновленный токен устарел");
        }

        return jwtService.generateToken(user);
    }
}
