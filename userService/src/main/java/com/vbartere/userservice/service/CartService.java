package com.vbartere.userservice.service;

import com.vbartere.userservice.DTO.AdvertisementDTO;
import com.vbartere.userservice.model.Cart;
import com.vbartere.userservice.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCartById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));
    }

    @Transactional
    public void deleteCart(Long id) {
        if (!cartRepository.existsById(id)) {
            throw new RuntimeException("Корзина не найдена");
        }
        cartRepository.deleteById(id);
    }

    @Transactional
    public Cart updateCart(Long id, Cart updatedCart) {
        if (!cartRepository.existsById(id)) {
            throw new RuntimeException("Корзина не найдена");
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

        System.out.println("Before adding: " + cart.getAdvertisementList());

        cart.getAdvertisementList().add(advertisementId);

        System.out.println("After adding: " + cart.getAdvertisementList());
        cartRepository.save(cart);
    }

    private Cart createNewCartForUser(Long userId) {
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        return cartRepository.save(newCart);
    }
}
