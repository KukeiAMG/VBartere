package com.example.ChatService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.security.Key;
import java.net.URI;
import java.util.Base64;

public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final String jwtSecret;


    private static final List<String> ALLOWED_ROLES = List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_DEV");

    public AuthHandshakeInterceptor(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        System.out.println("AuthHandshakeInterceptor---JWT---triggered");

        String token = null;

        // Получаем токен из query-параметра
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query != null && query.contains("token=")) {
            token = query.substring(query.indexOf("token=") + 6);
            int ampIndex = token.indexOf('&');
            if (ampIndex != -1) {
                token = token.substring(0, ampIndex);
            }
        }

        if (token == null || token.isEmpty()) {
            System.out.println("AuthHandshakeInterceptor---Token is missing");
            return false;
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Проверяем роли
            List<Map<String, Object>> roles = (List<Map<String, Object>>) claims.get("roles");
            boolean hasAllowedRole = roles.stream()
                    .map(role -> (String) role.get("name"))
                    .anyMatch(ALLOWED_ROLES::contains);

            if (!hasAllowedRole) {
                System.out.println("AuthHandshakeInterceptor---Role not allowed");
                return false;
            }

            // Сохраняем информацию о пользователе в атрибутах
            attributes.put("userId", claims.get("id"));
            attributes.put("phoneNumber", claims.get("phoneNumber"));
            attributes.put("roles", roles);

            System.out.println("userId " + claims.get("id"));
            System.out.println("phoneNumber "+ claims.get("phoneNumber"));
            System.out.println("roles "+ roles);

            return true;
        } catch (Exception e) {
            System.out.println("AuthHandshakeInterceptor---JWT error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Не требуется дополнительная логика
    }
}