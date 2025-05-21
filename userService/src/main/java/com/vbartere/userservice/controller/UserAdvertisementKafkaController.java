package com.vbartere.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.UserService.UserDTO;
import com.vbartere.Shared.Kafka.Events.CartEvent;
import com.vbartere.userservice.Kafka.Producers.SendCartRequest;
import com.vbartere.userservice.service.CartService;
import com.vbartere.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/advertisement")
@CrossOrigin(origins = "http://localhost:4200")
public class UserAdvertisementKafkaController {
    private final CartService cartService;
    private final UserService userService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final SendCartRequest sendCartRequest;

    private static final String TOPIC = "cart.events";

    public UserAdvertisementKafkaController(CartService cartService, UserService userService, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, SendCartRequest sendCartRequest) {
        this.cartService = cartService;
        this.userService = userService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.sendCartRequest = sendCartRequest;
    }

    @PostMapping("/{advertisementId}/add")
    public ResponseEntity<String> addAdvertisementToCart(@PathVariable(name = "advertisementId") Long advertisementId,
                                                         @RequestHeader(name = "Authorization") String jwtToken) {
        try {
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            Long userId = userService.getUserIdByToken(token);
            //cartService.addProductToCart(userId, advertisementId);
            UserDTO userDTO = userService.getById(userId);
            System.out.println("Controller userId = " + userId);

            CartEvent cartEvent = new CartEvent(userId, userDTO.isBanned(), advertisementId);
            sendCartRequest.sendCartRequest(objectMapper.writeValueAsString(cartEvent));

            return ResponseEntity.ok("Отправлен запрос на добавление в корзину");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при добавлении объявления в корзину");
        }
    }
}
