package com.vbartere.userservice.service;

import com.vbartere.userservice.Mapper.CartMapper;
import com.vbartere.Shared.Kafka.DTO.Cart.CartDTO;
import com.vbartere.userservice.model.Cart;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.CartRepository;
import com.vbartere.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartService(CartRepository cartRepository, UserRepository userRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.cartMapper = cartMapper;
    }

    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Корзина пользователя не найдена"));
        return cartMapper.toDto(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Корзина пользователя не найдена"));

        cart.setAdvertisementList(new ArrayList<>());
        cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCart(Long id, Cart updatedCart) {
        if (!cartRepository.existsById(id)) {
            throw new EntityNotFoundException("Корзина не найдена");
        }
        updatedCart.setId(id);
        return cartRepository.save(updatedCart);
    }

    @Transactional
    public void addProductToCart(Long userId, Long advertisementId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));

        if (cart.getAdvertisementList() == null) {
            cart.setAdvertisementList(new ArrayList<>());
        }

        if (!cart.getAdvertisementList().contains(advertisementId)) {
            System.out.println("Before adding: " + cart.getAdvertisementList());

            cart.getAdvertisementList().add(advertisementId);

            System.out.println("After adding: " + cart.getAdvertisementList());
        }

        cartRepository.save(cart);
    }

    private Cart createNewCartForUser(Long userId) {
        Cart newCart = new Cart();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден в БД")
        );
        newCart.setUser(user);
        newCart.setAdvertisementList(new ArrayList<>());

        return cartRepository.save(newCart);
    }
}
