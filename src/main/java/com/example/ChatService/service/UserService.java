package com.example.ChatService.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Base64;
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
    private final Set<String> activeUsers = new HashSet<>();

    /**
     * Проверяет существование пользователя.
     * 
     * @param username имя пользователя для проверки
     * @return true если пользователь существует, false в противном случае
     */
    public boolean userExists(String username) {
        return activeUsers.contains(username);
    }

    /**
     * Добавляет пользователя в список активных.
     * Вызывается при успешной аутентификации через JWT.
     * 
     * @param username имя пользователя
     */
    public void addActiveUser(String username) {
        activeUsers.add(username);
        logger.info("UserService---User {} added to active users", username);
    }

    /**
     * Удаляет пользователя из списка активных.
     * Вызывается при отключении пользователя.
     * 
     * @param username имя пользователя
     */
    public void removeActiveUser(String username) {
        activeUsers.remove(username);
        logger.info("UserService---User {} removed from active users", username);
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
    public String validateTokenAndGetUsername(String token) {
        try {
            
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();

            // Проверяем наличие имени пользователя
            String username = claims.getSubject();
            System.out.println(username);
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("UserService---Invalid token: username is empty");
            }

            // Проверяем срок действия токена
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                throw new IllegalArgumentException("UserService---Invalid token: expiration date is missing");
            }

            if (expiration.before(new Date())) {
                logger.warn("UserService---Token expired for user {} at {}", username, expiration);
                throw new TokenExpiredException("Token has expired");
            }

            return username;
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