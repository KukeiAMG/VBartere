package com.vbartere.userservice.controller;

import com.vbartere.Shared.Kafka.DTO.Cart.CartDTO;
import com.vbartere.userservice.exceptions.InvalidTokenException;
import com.vbartere.userservice.service.CartService;
import com.vbartere.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/my-cart")
    public ResponseEntity<?> getCartByUserId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InvalidTokenException("Невалидный формат токена");
            }

            String token = authHeader.substring(7);

            Long userId = userService.getUserIdByToken(token);

            CartDTO cartDTO = cartService.getCartByUserId(userId);
            return ResponseEntity.ok(cartDTO);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
