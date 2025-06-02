package com.example.Referral.kafka.consumer;

import com.example.Referral.service.DataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.UserReferralDTO;
import org.springframework.kafka.annotation.KafkaListener;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {
    private final DataService dataService;
    private final ObjectMapper objectMapper;

    public UserEventConsumer(DataService dataService, ObjectMapper objectMapper) {
        this.dataService = dataService;
        this.objectMapper = objectMapper;
    }

    // Обработчик сообщений из топика user.registration.referral
    @KafkaListener(topics = "user.registration.referral")
    public void handleUserCreated(String userJSON) {
        System.out.println("\n\n---UserEventConsumer---\n" + userJSON);

        try {
            // принимаю JSON и собираю в userDTO (десериализую)
            // Десериализация JSON в DTO
            // TODO: Нет проверки на null для userJSON
            UserReferralDTO user = objectMapper.readValue(userJSON, UserReferralDTO.class);


            System.out.println(user);

            // Регистрация пользователя через сервисный слой
            // TODO: Не обрабатывается случай, когда dataService.registerUser() бросает исключение
            dataService.registerUser(user.getId(), user.getInvitedByCode());

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
