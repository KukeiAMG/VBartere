package com.vbartere.AdminService.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.AdminService.Model.AdminUser;
import com.vbartere.AdminService.Repository.AdminAdvertisementRepository;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AdminAdvertisementEventConsumer {

    private final AdminAdvertisementRepository adminAdvertisementRepository;

    public AdminAdvertisementEventConsumer(AdminAdvertisementRepository adminAdvertisementRepository) {
        this.adminAdvertisementRepository = adminAdvertisementRepository;
    }

    @KafkaListener(topics = "advertisement-event")
    public void handleUserEvent(String message) throws JsonProcessingException {
        UserEvent event = new ObjectMapper().readValue(message, UserEvent.class);

        switch (event.getEvent().toString()) {
            // TODO сделать обработку событий у объявлений и тут создавать их копии для админки
        }
    }
}
