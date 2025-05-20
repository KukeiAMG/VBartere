package com.example.ChatService.service;

import com.example.ChatService.model.UserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.security.Key;
import io.jsonwebtoken.security.Keys;

/**
 * Сервис для работы с пользователями.
 * Хранит информацию о существующих пользователях на основе JWT токенов.
 * 
 * @author Your Name
 * @version 1.0
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Кэш активных пользователей
    private final Set<Long> activeUsers = new HashSet<>();

    /**
     * Проверяет существование пользователя.
     * 
     * @param userId ID пользователя для проверки
     * @return true если пользователь существует, false в противном случае
     */
    public boolean userExists(Long userId) {
        return activeUsers.contains(userId);
    }

    /**
     * Добавляет пользователя в список активных.
     * Вызывается при успешной аутентификации через JWT.
     * 
     * @param userId ID пользователя
     */
    public void addActiveUser(Long userId) {
        activeUsers.add(userId);
        logger.info("UserService---User with ID {} added to active users", userId);
    }

    /**
     * Удаляет пользователя из списка активных.
     * Вызывается при отключении пользователя.
     * 
     * @param userId ID пользователя
     */
    public void removeActiveUser(Long userId) {
        activeUsers.remove(userId);
        logger.info("UserService---User with ID {} removed from active users", userId);
    }

    /**
     * Проверяет валидность JWT токена и извлекает имя пользователя.
     * Выполняет следующие проверки:
     * 1. Валидность подписи токена
     * 2. Наличие имени пользователя в токене
     * 3. Срок действия токена (exp claim)
     * 
     * @param token JWT токен
     * @return имя пользователя из токена
     * @throws IllegalArgumentException если токен невалиден
     * @throws TokenExpiredException если срок действия токена истек
     */
    public UserDetails validateTokenAndGetUsername(String token) {
        try {
            
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();

            // Проверяем наличие имени пользователя
            UserDetails userDetails = new UserDetails();

            userDetails.setUserId(claims.get("id", Long.class));
            userDetails.setPhoneNumber(claims.get("phoneNumber", String.class));
            userDetails.setRoles((List<Map<String, Object>>) claims.get("roles"));

            // валидация полей
            System.out.println(userDetails.toString());
            if (userDetails.getUserId() == null) {
                throw new IllegalArgumentException("UserService---Invalid token: UserId is null");
            }

            if (userDetails.getPhoneNumber() == null || userDetails.getPhoneNumber().isEmpty()) {
                throw new IllegalArgumentException("UserService---Invalid token: phonenumber is null or empty");
            }

            if (userDetails.getRoles() == null || userDetails.getRoles().isEmpty()) {
                throw new IllegalArgumentException("UserService---Invalid token: role is null or empty");
            }

            // Проверяем срок действия токена
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                throw new IllegalArgumentException("UserService---Invalid token: expiration date is missing");
            }

            if (expiration.before(new Date())) {
                logger.warn("UserService---Token expired for user {} at {}", userDetails.getUserId(), expiration);
                throw new TokenExpiredException("Token has expired");
            }

            return userDetails;
        } catch (ExpiredJwtException e) {
            logger.warn("UserService---Token expired: {}", e.getMessage());
            throw new TokenExpiredException("Token has expired");
        } catch (Exception e) {
            logger.error("UserService---Token validation failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token");
        }
    }

    /**
     * Исключение, возникающее при истечении срока действия токена.
     */
    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String message) {
            super(message);
        }
    }
} 