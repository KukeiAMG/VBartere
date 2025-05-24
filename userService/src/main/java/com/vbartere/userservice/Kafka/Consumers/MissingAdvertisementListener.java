package com.vbartere.userservice.Kafka.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.CartResult;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import com.vbartere.userservice.Kafka.Producers.SendAdminRequest;
import com.vbartere.userservice.Kafka.Producers.SendNotificationRequest;
import com.vbartere.userservice.Mapper.AdminMapper;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.UserRepository;
import com.vbartere.userservice.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MissingAdvertisementListener {

    private final CartService cartService;
    private final ObjectMapper objectMapper;
    private final SendNotificationRequest sendNotificationRequest;
    private final UserRepository userRepository;
    private final SendAdminRequest sendAdminRequest;
    private final AdminMapper adminMapper;

    public MissingAdvertisementListener(CartService cartService, ObjectMapper objectMapper, SendNotificationRequest sendNotificationRequest, UserRepository userRepository, SendAdminRequest sendAdminRequest, AdminMapper adminMapper) {
        this.cartService = cartService;
        this.objectMapper = objectMapper;
        this.sendNotificationRequest = sendNotificationRequest;
        this.userRepository = userRepository;
        this.sendAdminRequest = sendAdminRequest;
        this.adminMapper = adminMapper;
    }

    @KafkaListener(topics = "missing.advertisements")
    public void handleCartEvent(String message) throws JsonProcessingException {
        System.out.println(message);
        CartResult result = objectMapper.readValue(message, CartResult.class);
        System.out.println("____________________________________"+result+"_________________________________");

        User user;
        try {
            user = userRepository.findByIdWithCartAndAds(result.getUserId()).orElseThrow(
                    () -> new EntityNotFoundException("Пользователь не найден в БД")
            );
        } catch (EntityNotFoundException e) {
            if (result.getEventType() == UserEventType.USER_REMOVE_HIS_ADVERTISEMENT) {
                System.out.println("Вы успешно удалили свое объявление " + result.getAdvertisementId());
                cartService.removeAdvertisementFromAllCarts(result.getAdvertisementId());

                UserEvent userEvent = new UserEvent(
                        result.getUserId(),
                        "Удалённый пользователь",
                        "unknown@system",
                        UserEventType.USER_REMOVE_HIS_ADVERTISEMENT
                );
                userEvent.setDescription("Пользователь удалил аккаунт и свои объявления");

                sendNotificationRequest.sendRequest(objectMapper.writeValueAsString(userEvent));
                return;
            } else {
                return;
            }
        }
        AdminUserDTO adminUserDTO = adminMapper.toDto(user, UserEventType.USER_UPDATED);

        if (!user.isBanned()) {
            if (result.isSuccessfullyProcessed()) {
                switch (result.getEventType()) {
                    case USER_ADD_ADVERTISEMENT_TO_CART -> {
                        System.out.println("Объявление с ID " + result.getAdvertisementId() + " успешно обработано.");
                        System.out.println("Цена объявления: " + result.getPrice());
                        cartService.addProductToCart(result.getUserId(), result.getAdvertisementId(), result.getPrice());

                        UserEvent userEvent = new UserEvent(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                UserEventType.USER_ADD_ADVERTISEMENT_TO_CART
                        );
                        userEvent.setDescription("Объявление успешно добавлено");
                        sendNotificationRequest.sendRequest(objectMapper.writeValueAsString(userEvent));
                        sendAdminRequest.updateAdminAsync(adminUserDTO);
                    }
                    case USER_REMOVE_ADVERTISEMENT_FROM_CART -> {
                        System.out.println("Объявление с ID " + result.getAdvertisementId() + " успешно обработано.");
                        cartService.removeProductFromCart(result.getUserId(), result.getAdvertisementId());
                        UserEvent userEvent = new UserEvent(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                UserEventType.USER_REMOVE_ADVERTISEMENT_FROM_CART
                        );
                        userEvent.setDescription("Объявление успешно удалено");
                        sendNotificationRequest.sendRequest(objectMapper.writeValueAsString(userEvent));
                        sendAdminRequest.updateAdminAsync(adminUserDTO);
                    }
                    case USER_CLEARED_HIS_CART -> {
                        System.out.println("Корзина успешно очищена");
                        cartService.clearCart(result.getUserId());
                        UserEvent userEvent = new UserEvent(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                UserEventType.USER_CLEARED_HIS_CART
                        );
                        userEvent.setDescription("Корзина успешно очищена");
                        sendNotificationRequest.sendRequest(objectMapper.writeValueAsString(userEvent));
                        adminUserDTO.getAddedAdvertisements().clear();
                        System.out.println(adminUserDTO);
                        sendAdminRequest.updateAdminAsync(adminUserDTO);
                    }
                    case USER_DID_NOT_FIND_THE_ADVERTISEMENT -> {
                        System.out.println("Не удалось найти объявление с ID " + result.getAdvertisementId());

                        UserEvent userEvent = new UserEvent(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                UserEventType.USER_DID_NOT_FIND_THE_ADVERTISEMENT
                        );
                        userEvent.setDescription("Не удалось найти объявление");
                        sendNotificationRequest.sendRequest(objectMapper.writeValueAsString(userEvent));
                    }
                    case USER_REMOVE_HIS_ADVERTISEMENT -> {
                        System.out.println("Вы успешно удалили свое объявление " + result.getAdvertisementId());
                        cartService.removeAdvertisementFromAllCarts(result.getAdvertisementId());
                        UserEvent userEvent = new UserEvent(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                UserEventType.USER_REMOVE_HIS_ADVERTISEMENT
                        );
                        userEvent.setDescription("Не удалось найти объявление");
                        sendAdminRequest.updateAdminAsync(adminUserDTO);
                        sendNotificationRequest.sendRequest(objectMapper.writeValueAsString(userEvent));
                    }
                }
            }
        } else {
            System.out.println("Вы заблокированы и не можете добавлять объявления " + result.getAdvertisementId());
            UserEvent userEvent = new UserEvent(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    UserEventType.USER_BANNED
            );
            userEvent.setDescription("Вы заблокированы и не можете добавлять объявления");
            sendNotificationRequest.sendRequest(objectMapper.writeValueAsString(userEvent));
        }
    }
}
