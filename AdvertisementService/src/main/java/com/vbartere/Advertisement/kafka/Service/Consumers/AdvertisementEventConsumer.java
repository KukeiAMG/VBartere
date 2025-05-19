package com.vbartere.Advertisement.kafka.Service.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import com.vbartere.Shared.Kafka.Enum.AdvertisementEventType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdvertisementEventConsumer {

    private final AdvertisementRepository advertisementRepository;
    private final ObjectMapper objectMapper;

    private final String TOPIC = "advertisement.event";

    public AdvertisementEventConsumer(AdvertisementRepository advertisementRepository, ObjectMapper objectMapper) {
        this.advertisementRepository = advertisementRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = TOPIC)
    public void handleAdvertisementEvent(String message) throws JsonProcessingException {
        AdminAdvertisementDTO event = objectMapper.readValue(message, AdminAdvertisementDTO.class);

        if (event.getEventType().equals(AdvertisementEventType.ADVERTISEMENT_BANNED)) {
            advertisementRepository.deleteById(event.getId());

            // TODO notifyUser
        }
    }
}
