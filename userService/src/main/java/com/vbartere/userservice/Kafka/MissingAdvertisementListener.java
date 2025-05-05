package com.vbartere.userservice.Kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vbartere.Shared.Kafka.Events.CartResult;
import com.vbartere.userservice.service.CartService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MissingAdvertisementListener {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    public MissingAdvertisementListener(CartService cartService, ObjectMapper objectMapper) {
        this.cartService = cartService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "missing-advertisements", groupId = "advertisement-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleCartEvent(String message) throws JsonProcessingException {
        CartResult result = objectMapper.readValue(message, CartResult.class);
        if (result.isSuccessful()) {
            System.out.println("Объявление с ID " + result.getAdvertisementId() + " успешно обработано.");
            // TODO notifyUser("Объявление успешно обработано.");
            cartService.addProductToCart(result.getUserId(), result.getAdvertisementId());
        } else {
            System.out.println("Не удалось обработать объявление с ID " + result.getAdvertisementId());
            // TODO notifyUser("Объявление не найдено.");
        }
    }
}
