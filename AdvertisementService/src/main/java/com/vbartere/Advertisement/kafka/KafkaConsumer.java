package com.vbartere.Advertisement.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.AdvertisementDTO;
//import com.vbartere.Shared.Kafka.DTO.AdvertisementMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Service.AdvertisementService;

import com.vbartere.Shared.Kafka.CartEvent;
import com.vbartere.Shared.Kafka.CartResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class KafkaConsumer {

    private final AdvertisementService advertisementService;
    private final AdvertisementRepository advertisementRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaConsumer(AdvertisementService advertisementService, AdvertisementRepository advertisementRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.advertisementService = advertisementService;
        this.advertisementRepository = advertisementRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "cart-events", groupId = "advertisement-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleCartEvent(CartEvent event) {
        try {
            Advertisement advertisement = advertisementService.getAdvertisementById(event.getAdvertisementId());
            AdvertisementDTO advertisementDTO = objectMapper.convertValue(advertisement, AdvertisementDTO.class);

            if (advertisementDTO.isStatus()) {
                advertisementDTO.setBuyersId(event.getUserId());
                advertisementDTO.setStatus(false);

                Advertisement existingEntity = advertisementService.getAdvertisementById(event.getAdvertisementId());
                existingEntity.setBuyersId(event.getUserId());
                existingEntity.setStatus(false);
                advertisementRepository.save(existingEntity);

                System.out.println("\nОбработка события: " + event + "\nТовар: " + advertisementDTO);
                kafkaTemplate.send("missing-advertisements", String.valueOf(new CartResult(event.getUserId(), event.getAdvertisementId(), true)));
            }
            else {
                System.out.println("Данное объявление снято с публикации");
                kafkaTemplate.send("missing-advertisements", String.valueOf(new CartResult(event.getUserId(), event.getAdvertisementId(), false)));
            }

        } catch (EntityNotFoundException e) {
            // Обработка случая, когда объявление не найдено
            System.out.println("Объявление с ID " + event.getAdvertisementId() + " не найдено.");

            String message = "Объявление с ID " + event.getAdvertisementId() + " не найдено.";
            kafkaTemplate.send("missing-advertisements", String.valueOf(new CartResult(event.getUserId(), event.getAdvertisementId(), false)));
            //kafkaTemplate.send("missing-advertisements", message);

            // TODO notifyUser("Объявление не найдено.");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}