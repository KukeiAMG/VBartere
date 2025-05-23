package com.vbartere.Gateway.Clients;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.vbartere.Shared.Kafka.DTO.Gateway.UserInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public class UserInfoClient {
    private final WebClient webClient;
    private final Cache<String, UserInfoDTO> cache;

    @Value("${user.service.address}")
    private String USER_SERVICE_ADDRESS;

    public UserInfoClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(USER_SERVICE_ADDRESS).build();
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(1000)
                .build();
    }

    public Mono<UserInfoDTO> getUserInfo(String token) {
        return webClient.get()
                .uri(USER_SERVICE_ADDRESS + "/api/jwt/currentUserInfo")
                .headers(headers -> {
                    headers.setBearerAuth(token);
                    headers.set("X-Gateway-Secret", "my-super-secret");
                })
                .retrieve()
                .bodyToMono(UserInfoDTO.class)
                .doOnNext(userInfo -> cache.put(token, userInfo));
    }
}
