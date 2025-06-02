package com.example.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Controller
public class GetOwnerAdvertisements {
    private final RestTemplate restTemplate;

    @Value("${services.advertisement.url}")
    private String advertisementServiceUrl;

    public GetOwnerAdvertisements(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<Long, Long> getOwnerAdvertisements(List<Long> advertisementIds) {
        // Установка заголовков
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Internal-Call", "payment-service-secret"); // Кастомный заголовок

        // Создаем HTTP-сущность с телом и заголовками
        HttpEntity<List<Long>> entity = new HttpEntity<>(advertisementIds, headers);

        try{
            // Отправка запроса и получение ответа
            ResponseEntity<Map<Long, Long>> response = restTemplate.exchange(
                    advertisementServiceUrl + "/api/advertisements/owners",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {} // Для сложных типов
            );

            // 5. Проверка статуса и возврат результата
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Ошибка получения продавцов. Status: " + response.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Advertisement service недоступен", e);
        }

    }
}
