package com.vbartere.userservice.service;
import com.vbartere.userservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("roles", user.getRoles()); // например: ["ROLE_USER", "ROLE_ADMIN"]
        return createToken(claims, user.getPhoneNumber(), 10 * 60 * 60 * 1000); // 10 часов
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("roles", user.getRoles());
        return createToken(claims, user.getPhoneNumber(), 7 * 24 * 60 * 60 * 1000); // 7 дней
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // <-- используем ключ Key, а не строку
                .compact();
    }


    public String extractPhoneNumber(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Преобразование ключа в байты
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Проверка токена на соответствие пользователю и проверка срока действия
    public Boolean validateToken(String token, String phoneNumber) {
        final String extractedPhoneNumber = extractPhoneNumber(token);
        return (extractedPhoneNumber.equals(phoneNumber) && !isTokenExpired(token));
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Transactional(readOnly = true)
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object roles = claims.get("roles");

        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .map(roleObj -> {
                        if (roleObj instanceof Map) {
                            // Приводим к Map и вытаскиваем поле name
                            Map<?, ?> roleMap = (Map<?, ?>) roleObj;
                            Object name = roleMap.get("name");
                            return name != null ? name.toString() : "";
                        }
                        // Если не Map — возвращаем toString (на всякий случай)
                        return roleObj.toString();
                    })
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.toList());
        }

        return List.of();
    }

}

