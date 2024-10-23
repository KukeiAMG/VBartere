package com.vbartere.Gateway.Config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class GatewayConfig {

    private final RestTemplate restTemplate;

    public GatewayConfig(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Bean
    public RouteLocator CustomRoute (RouteLocatorBuilder builder) {
        return builder.routes()
                .route("AdvertisementService", r -> r.path("/api/advertisements/**")
                        .filters(f -> f.filter((exchange, chain) -> {
                            // Проверка JWT и phoneNumber
                            // В Headers добавляем ключи Authorization и phoneNumber, а значения - токен и телефон юзера
                            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                            String phoneHeader = exchange.getRequest().getHeaders().getFirst("phoneNumber");

                            assert phoneHeader != null;

                            if (authHeader != null && authHeader.startsWith("Bearer")) {
                                String jwtToken = authHeader.substring(7);
                                String phoneNumber = phoneHeader.substring(7);

                                if (validateJwt(jwtToken, phoneNumber)) {
                                    return chain.filter(exchange);
                                }
                            }
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }))
                        .uri("http://localhost:8080")) // URL AdvertisementService
                .build();
    }

    private boolean validateJwt(String jwtToken, String phoneNumber) {
        try {
            // Создание URL с параметрами запроса для валидации
            String url = UriComponentsBuilder.fromHttpUrl("http://localhost:8081/api/users/validate")
                    .queryParam("token", jwtToken)
                    .queryParam("phoneNumber", phoneNumber)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            // Создание HttpEntity без тела, если не нужно передавать дополнительные данные в теле запроса
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Boolean.class);

            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
