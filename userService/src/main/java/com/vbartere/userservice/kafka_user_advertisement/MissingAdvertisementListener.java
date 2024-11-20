package com.vbartere.userservice.kafka_user_advertisement;

import com.vbartere.Shared.Kafka.CartResult;
import com.vbartere.userservice.service.CartService;
import com.vbartere.userservice.service.UserService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MissingAdvertisementListener {

    private final CartService cartService;
    private final UserService userService;

    public MissingAdvertisementListener(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @KafkaListener(topics = "missing-advertisements", groupId = "advertisement-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleCartEvent(CartResult result) {
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
