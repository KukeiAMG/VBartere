package com.vbartere.Gateway.Config;

import com.vbartere.Gateway.Filter.JwtGatewayFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewayConfig {

    @Value("${advertisement.service.address}")
    private String ADVERTISEMENT_SERVICE_ADDRESS;

    @Value("${user.service.address}")
    private String USER_SERVICE_ADDRESS;

    @Value("${admin.service.address}")
    private String ADMIN_SERVICE_ADDRESS;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, JwtGatewayFilter jwtGatewayFilter) {
        return builder.routes()
                .route("AdminService", r -> r
                        .path("/api/admin/**")
                        .filters(f -> f
                                .filter(jwtGatewayFilter)
                        )
                        .uri(ADMIN_SERVICE_ADDRESS)
                )
                .build();
    }

}