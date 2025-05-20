package com.example.ChatService.security;

import com.example.ChatService.model.UserDetails;
import com.example.ChatService.service.UserService;
import com.example.ChatService.service.UserService.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Перехватчик WebSocket соединений для аутентификации пользователей.
 * Проверяет JWT токен в заголовке Authorization при установке WebSocket соединения.
 * 
 * @author Your Name
 * @version 1.0
 */
@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthChannelInterceptor.class);

    private final UserService userService;

    public AuthChannelInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("token");

            if (token == null) {
                Object rawUri = accessor.getHeader("simpConnectMessage");
                if (rawUri != null && rawUri.toString().contains("token=")) {
                    String uri = rawUri.toString();
                    int tokenStart = uri.indexOf("token=") + 6;
                    int tokenEnd = uri.indexOf('&', tokenStart);
                    token = tokenEnd != -1 ? uri.substring(tokenStart, tokenEnd) : uri.substring(tokenStart);
                }
            }

            if (token == null) {
                logger.error("AuthChannelInterceptor---Token is missing");
                return null;
            }

            try {
                UserDetails userDetails = userService.validateTokenAndGetUsername(token);
                userService.addActiveUser(userDetails.getUserId());
                
                accessor.setUser(new java.security.Principal() {
                    @Override
                    public String getName() {
                        return userDetails.getUserId().toString();
                    }
                });
                
                logger.info("AuthChannelInterceptor---User with ID {} authenticated successfully", userDetails.getUserId());
            } catch (TokenExpiredException e) {
                logger.warn("AuthChannelInterceptor---Token expired: {}", e.getMessage());
                accessor.setHeader("token-expired", true);
                return null;
            } catch (Exception e) {
                logger.error("AuthChannelInterceptor---JWT validation failed: {}", e.getMessage());
                return null;
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                try {
                    Long userId = Long.parseLong(accessor.getUser().getName());
                    userService.removeActiveUser(userId);
                } catch (NumberFormatException e) {
                    logger.error("AuthChannelInterceptor---Invalid user ID format: {}", accessor.getUser().getName());
                }
            }
        }
        return message;
    }
} 