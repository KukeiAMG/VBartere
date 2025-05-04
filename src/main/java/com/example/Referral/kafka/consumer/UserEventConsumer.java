package com.example.Referral.kafka.consumer;

import com.example.Referral.kafka.DTO.UserEventDTO;
import com.example.Referral.service.DataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {
    private final DataService dataService;
    private final ObjectMapper objectMapper;

    public UserEventConsumer(DataService dataService, ObjectMapper objectMapper) {
        this.dataService = dataService;
        this.objectMapper = objectMapper;
    }

    // Обработчик сообщений из топика user-created
    @KafkaListener(topics = "user-created", groupId = "referral-service-group")
    public void handleUserCreated(String userJSON) {


        System.out.println("---UserEventConsumer---\n" + userJSON);

        try {
            // принимаю JSON и собираю в userDTO (десериализую)
            // Десериализация JSON в DTO
            // TODO: Нет проверки на null для userJSON
            UserEventDTO user = objectMapper.readValue(userJSON, UserEventDTO.class);

            System.out.println(user);

            // Регистрация пользователя через сервисный слой
            // TODO: Не обрабатывается случай, когда dataService.registerUser() бросает исключение
            dataService.registerUser(user.getUserId(), user.getInvitedByCode());
            System.out.println(user);

        } catch (JsonProcessingException e) {
            // Логируем ошибку, но не прерываем выполнение
            System.err.println("Ошибка при обработке сообщения: " + e.getMessage());
            // TODO: Реализовать dead-letter queue
            // Можно добавить дополнительную логику обработки ошибки, например:
            // - отправить сообщение в dead-letter queue
            // - записать в лог ошибок
            // - увеличить счетчик метрик ошибок
        } catch (Exception e) {
            // Ловим любые другие исключения, которые могут возникнуть
            System.err.println("Неожиданная ошибка при обработке сообщения: " + e.getMessage());
        }
    }
}
