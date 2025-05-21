package com.vbartere.userservice.Kafka.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.CartResult;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import com.vbartere.userservice.Kafka.Producers.SendNotificationRequest;
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

    public MissingAdvertisementListener(CartService cartService, ObjectMapper objectMapper, SendNotificationRequest sendNotificationRequest, UserRepository userRepository) {
        this.cartService = cartService;
        this.objectMapper = objectMapper;
        this.sendNotificationRequest = sendNotificationRequest;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "missing.advertisements")
    public void handleCartEvent(String message) throws JsonProcessingException {
        CartResult result = objectMapper.readValue(message, CartResult.class);

        User user = userRepository.findById(result.getUserId()).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден в БД")
        );

        if (!user.isBanned()) {
            if (result.isSuccessful()) {
                System.out.println("Объявление с ID " + result.getAdvertisementId() + " успешно обработано.");
                cartService.addProductToCart(result.getUserId(), result.getAdvertisementId());
                UserEvent userEvent = new UserEvent(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        UserEventType.USER_ADD_ADVERTISEMENT_TO_CART
                );
                userEvent.setDescription("Объявление успешно добавлено");
                sendNotificationRequest.sendNotificationRequest(objectMapper.writeValueAsString(userEvent));
            } else {
                System.out.println("Не удалось обработать объявление с ID " + result.getAdvertisementId());
                UserEvent userEvent = new UserEvent(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        UserEventType.USER_DID_NOT_FIND_THE_ADVERTISEMENT
                );
                userEvent.setDescription("Не удалось найти объявление");
                sendNotificationRequest.sendNotificationRequest(objectMapper.writeValueAsString(userEvent));
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
            sendNotificationRequest.sendNotificationRequest(objectMapper.writeValueAsString(userEvent));
        }
    }
}
