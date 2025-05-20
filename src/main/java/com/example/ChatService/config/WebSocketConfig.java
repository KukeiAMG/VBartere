package com.example.ChatService.config;

import com.example.ChatService.security.AuthChannelInterceptor;
import com.example.ChatService.security.AuthHandshakeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.beans.factory.annotation.Value;

/**
 * Конфигурация WebSocket для чат-сервиса.
 * Настраивает STOMP брокер, эндпоинты и перехватчики сообщений.
 * 
 * @author KukeiAMG
 * @version 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authInterceptor;
    private final String jwtSecret;
    
    // Константы для настройки размеров сообщений
    private static final int MAX_TEXT_MESSAGE_SIZE = 8192; // 8KB
    private static final int MAX_BINARY_MESSAGE_SIZE = 8192; // 8KB
    private static final int MAX_FRAME_SIZE = 16384; // 16KB
    private static final int SEND_BUFFER_SIZE = 512 * 1024; // 512KB
    private static final int RECEIVE_BUFFER_SIZE = 512 * 1024; // 512KB
    private static final int MESSAGE_SIZE_LIMIT = 64 * 1024; // 64KB

    public WebSocketConfig(AuthChannelInterceptor authInterceptor, @Value("${jwt.secret}") String jwtSecret) {
        this.authInterceptor = authInterceptor;
        this.jwtSecret = jwtSecret;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Для обычного WebSocket
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new AuthHandshakeInterceptor(jwtSecret));
        // Для SockJS
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new AuthHandshakeInterceptor(jwtSecret))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
        registration.taskExecutor(webSocketTaskExecutor());
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(MESSAGE_SIZE_LIMIT)
                   .setSendBufferSizeLimit(SEND_BUFFER_SIZE)
                   .setSendTimeLimit(20000) // 20 секунд
                   .setTimeToFirstMessage(30000); // 30 секунд
    }

    /**
     * Создает пул потоков для обработки WebSocket сообщений.
     * 
     * @return настроенный ThreadPoolTaskExecutor
     */
    @Bean
    public ThreadPoolTaskExecutor webSocketTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ws-");
        executor.initialize();
        return executor;
    }
} 