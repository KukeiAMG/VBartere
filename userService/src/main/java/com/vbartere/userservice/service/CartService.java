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
import com.vbartere.userservice.model.Embeddable.CartItem;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.CartRepository;
import com.vbartere.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public void addProductToCart(Long userId, Long advertisementId, BigDecimal priceAtMoment) {

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

        boolean alreadyExists = cart.getAdvertisementList().stream()
                .anyMatch(item -> item.getAdvertisementId().equals(advertisementId));

        if (!alreadyExists) {
            CartItem newItem = new CartItem();
            newItem.setAdvertisementId(advertisementId);
            newItem.setPrice(priceAtMoment);
            newItem.setSelected(true);

            cart.getAdvertisementList().add(newItem);

            System.out.println("Товар добавлен в корзину: " + advertisementId);
        } else {
            System.out.println("Товар уже есть в корзине: " + advertisementId);
        }

        cartRepository.save(cart);
    }

    @Transactional
    public void removeProductFromCart(Long userId, Long advertisementId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден в БД")
        );

        Cart cart = user.getCart();
        if (cart == null || cart.getAdvertisementList() == null) {
            System.out.println("У пользователя нет корзины или она пуста.");
            return;
        }

        List<CartItem> items = cart.getAdvertisementList();

        boolean removed = items.removeIf(item -> advertisementId.equals(item.getAdvertisementId()));

        if (removed) {
            System.out.println("Объявление " + advertisementId + " удалено из корзины.");
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

        List<CartItem> advertisementList = cart.getAdvertisementList();
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
            List<CartItem> items = cart.getAdvertisementList();
            boolean removed = items.removeIf(item -> item.getAdvertisementId().equals(advertisementId));
            if (removed) {
                affected++;
            }
        }

        cartRepository.saveAll(carts);
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
