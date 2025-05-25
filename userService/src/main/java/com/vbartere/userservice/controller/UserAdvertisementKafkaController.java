package com.vbartere.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.Cart.CartDTO;
import com.vbartere.Shared.Kafka.DTO.Embeddable.CartItemDTO;
import com.vbartere.Shared.Kafka.DTO.PaymentService.PaymentDTO;
import com.vbartere.Shared.Kafka.DTO.UserService.UserDTO;
import com.vbartere.Shared.Kafka.Enum.CartEventType;
import com.vbartere.Shared.Kafka.Events.CartEvent;
import com.vbartere.userservice.Kafka.Producers.SendCartRequest;
import com.vbartere.userservice.Kafka.Producers.SendPaymentRequest;
import com.vbartere.userservice.service.CartService;
import com.vbartere.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/advertisement")
@CrossOrigin(origins = "http://localhost:4200")
public class UserAdvertisementKafkaController {
    private final CartService cartService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final SendCartRequest sendCartRequest;
    private final SendPaymentRequest sendPaymentRequest;

    public UserAdvertisementKafkaController(CartService cartService, UserService userService, ObjectMapper objectMapper, SendCartRequest sendCartRequest, SendPaymentRequest sendPaymentRequest) {
        this.cartService = cartService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.sendCartRequest = sendCartRequest;
        this.sendPaymentRequest = sendPaymentRequest;
    }

    @PostMapping("/{advertisementId}/add")
    public ResponseEntity<String> addAdvertisementToCart(@PathVariable(name = "advertisementId") Long advertisementId,
                                                         @RequestHeader(name = "Authorization") String jwtToken) {
        try {
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            Long userId = userService.getUserIdByToken(token);

            UserDTO userDTO = userService.getById(userId);
            System.out.println("Controller userId = " + userId);

            CartEvent cartEvent = new CartEvent(userId,
                    userDTO.isBanned(),
                    advertisementId,
                    BigDecimal.ZERO,
                    CartEventType.ADD_ADVERTISEMENT_TO_CART
            );
            sendCartRequest.sendRequest(objectMapper.writeValueAsString(cartEvent));

            return ResponseEntity.ok("Отправлен запрос на добавление в корзину");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при добавлении объявления в корзину");
        }
    }

    @PostMapping("/advertisements/confirm-purchase")
    public ResponseEntity<?> confirmPurchased(@RequestHeader(name = "Authorization") String jwtToken) {
        try {
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            Long userId = userService.getUserIdByToken(token);

            CartDTO cartDTO = cartService.getCartByUserId(userId);

            Map<Long, BigDecimal> advertisementsWithPrice = new HashMap<>();
            for (CartItemDTO cartItem : cartDTO.getAdvertisementIds()) {
                if (cartItem.getSelected()) {
                    advertisementsWithPrice.put(
                            cartItem.getAdvertisementId(),
                            cartItem.getPrice()
                    );
                }
            }

            PaymentDTO paymentDTO = new PaymentDTO(
                    userId,
                    advertisementsWithPrice
            );

            sendPaymentRequest.sendRequest(objectMapper.writeValueAsString(paymentDTO));

            return ResponseEntity.ok("Обработка платежа...");
        } catch(EntityNotFoundException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{advertisementId}/remove")
    public ResponseEntity<String> removeAdvertisementFromCart(@PathVariable(name = "advertisementId") Long advertisementId,
                                                         @RequestHeader(name = "Authorization") String jwtToken) {
        try {
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            Long userId = userService.getUserIdByToken(token);

            CartDTO cartDTO = cartService.getCartByUserId(userId);

            UserDTO userDTO = userService.getById(userId);
            System.out.println("Controller userId = " + userId);

            CartEvent cartEvent = new CartEvent(userId,
                    userDTO.isBanned(),
                    advertisementId,
                    cartDTO.getAdvertisementIds().get(Math.toIntExact(advertisementId)).getPrice(),
                    CartEventType.REMOVE_ADVERTISEMENT_FROM_CART
            );
            sendCartRequest.sendRequest(objectMapper.writeValueAsString(cartEvent));

            return ResponseEntity.ok("Отправлен запрос на удаления объявления");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при добавлении объявления в корзину");
        }
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<String> clearCart(@RequestHeader("Authorization") String jwtToken) {
        try {
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
            Long userId = userService.getUserIdByToken(token);

            UserDTO userDTO = userService.getById(userId);
            System.out.println("Controller userId = " + userId);

            CartEvent cartEvent = new CartEvent(
                    userId,
                    userDTO.isBanned(),
                    null,
                    null,
                    CartEventType.CLEAR_CART
            );
            sendCartRequest.sendRequest(objectMapper.writeValueAsString(cartEvent));

            return ResponseEntity.ok("Корзина успешно очищена.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при очистке корзины.");
        }
    }
}
