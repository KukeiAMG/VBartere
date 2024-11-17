package com.vbartere.userservice.controller;
import com.vbartere.Shared.Kafka.CartEvent;


import com.vbartere.userservice.service.CartService;
import com.vbartere.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserAdvertisementController {

    private final CartService cartService;
    private final UserService userService;

    @Autowired
    private KafkaTemplate<String, CartEvent> kafkaTemplate;
    private static final String TOPIC = "cart-events";

    public UserAdvertisementController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @PostMapping("/api/advertisements/{advertisementId}/add")
    public ResponseEntity<String> addAdvertisementToCart(@PathVariable(name = "advertisementId") Long advertisementId,
                                                         @RequestHeader(name = "Authorization") String jwtToken) {
        try {
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            Long userId = userService.getUserIdByPhoneNumber(token);
            cartService.addProductToCart(userId, advertisementId);

            System.out.println("Controller userId = " + userId);

            CartEvent cartEvent = new CartEvent(userId, advertisementId);
            kafkaTemplate.send(TOPIC, cartEvent);

            return ResponseEntity.ok("Объявление успешно добавлено в корзину");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при добавлении объявления в корзину");
        }
    }
}
