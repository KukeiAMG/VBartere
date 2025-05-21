package com.vbartere.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.CartEvent;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import com.vbartere.userservice.Kafka.Producers.SendCartRequest;
import com.vbartere.userservice.Kafka.Producers.SendNotificationRequest;
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
import java.util.List;

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
    public Cart updateCart(Long id, Cart updatedCart) {
        if (!cartRepository.existsById(id)) {
            throw new EntityNotFoundException("Корзина не найдена");
        }
        updatedCart.setId(id);
        return cartRepository.save(updatedCart);
    }

    @Transactional
    public void addProductToCart(Long userId, Long advertisementId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден в БД")
        );

        if (user.isBanned()) {
            throw new IllegalStateException("Пользователь заблокирован");
        }

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

    @Transactional
    public void removeProductFromCart(Long userId, Long advertisementId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден в БД")
        );

        Cart cart = user.getCart();

        List<Long> advertisementList = cart.getAdvertisementList();

        if (advertisementList != null && advertisementList.contains(advertisementId)) {
            System.out.println("Before removing: " + advertisementList);

            advertisementList.remove(advertisementId);

            System.out.println("After removing: " + advertisementList);

            cartRepository.save(cart);
        } else {
            System.out.println("Объявление не найдено в корзине пользователя.");
        }
    }

    @Transactional
    public void clearCart(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Cart cart = user.getCart();
        if (cart == null) {
            System.out.println("Корзина пользователя " + userId + " не найдена.");
            return;
        }

        List<Long> advertisementList = cart.getAdvertisementList();
        if (advertisementList != null && !advertisementList.isEmpty()) {
            System.out.println("Корзина до очистки: " + advertisementList);
            advertisementList.clear(); // Очищаем весь список
            cartRepository.save(cart); // Сохраняем изменения
            System.out.println("Корзина пользователя " + userId + " успешно очищена.");
        } else {
            System.out.println("Корзина пользователя " + userId + " уже пуста.");
        }
    }

    @Transactional
    public void removeAdvertisementFromAllCarts(Long advertisementId) {
        List<Cart> carts = cartRepository.findByAdvertisementListContaining(advertisementId);

        int affected = 0;
        for (Cart cart : carts) {
            List<Long> ads = cart.getAdvertisementList();
            if (ads.remove(advertisementId)) {
                affected++;
            }
        }

        cartRepository.saveAll(carts); // Сохраняем изменённые корзины
        System.out.println("Объявление " + advertisementId + " удалено из " + affected + " корзин.");
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
