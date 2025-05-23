package com.vbartere.Gateway.Filter;

import com.vbartere.Gateway.Clients.UserInfoClient;
import com.vbartere.Shared.Kafka.DTO.Gateway.UserInfoDTO;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
public class JwtGatewayFilter implements GatewayFilter {
    private final UserInfoClient userInfoClient;

    public JwtGatewayFilter(UserInfoClient userInfoClient) {
        this.userInfoClient = userInfoClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        return userInfoClient.getUserInfo(token)
                .flatMap(userInfo -> {
                    exchange.getAttributes().put("USER_INFO", userInfo);

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .headers(httpHeaders -> {
                                httpHeaders.keySet().removeIf(key -> key.startsWith("Forwarded") || key.startsWith("X-Forwarded"));
                                httpHeaders.remove(HttpHeaders.AUTHORIZATION);
                                httpHeaders.set("X-User-Id", userInfo.getId().toString());

                                if (userInfo.getRoles() != null && !userInfo.getRoles().isEmpty()) {
                                    httpHeaders.set("X-User-Roles", String.join(",", userInfo.getRoles()));
                                }
                            })
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> unauthorized(exchange));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

}