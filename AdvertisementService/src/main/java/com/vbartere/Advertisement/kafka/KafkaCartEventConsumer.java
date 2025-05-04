package com.vbartere.Advertisement.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Advertisement.kafka.Service.SendCacheService;
import com.vbartere.Shared.Kafka.Events.CartEvent;
import com.vbartere.Shared.Kafka.Events.CartResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class KafkaCartEventConsumer {

    private final AdvertisementService advertisementService;
    private final AdvertisementRepository advertisementRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final SendCacheService sendCacheService;

    public KafkaCartEventConsumer(AdvertisementService advertisementService, AdvertisementRepository advertisementRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, SendCacheService sendCacheService) {
        this.advertisementService = advertisementService;
        this.advertisementRepository = advertisementRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.sendCacheService = sendCacheService;
    }

    @KafkaListener(topics = "cart-events", groupId = "advertisement-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleCartEvent(String message) throws JsonProcessingException {
        CartEvent event = objectMapper.readValue(message, CartEvent.class);
        try {
            Advertisement cachedAdvertisement = advertisementService.getAdvertisementById(event.getAdvertisementId());

            if (cachedAdvertisement.getStatus()) {
                System.out.println("Объявление доступно");

                // Получаем managed-сущность из БД
                Advertisement persistentAd = advertisementRepository.findById(event.getAdvertisementId())
                        .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

                persistentAd.setBuyersId(event.getUserId());
                persistentAd.setStatus(false);
                advertisementRepository.save(persistentAd);

                sendCacheService.sendCacheRequest(objectMapper.writeValueAsString(persistentAd));

                System.out.println("\nОбработка события: " + event + "\nТовар: " + persistentAd.getId());
                kafkaTemplate.send("missing-advertisements", objectMapper.writeValueAsString(new CartResult(event.getUserId(), event.getAdvertisementId(), true)));
            }
            else {
                System.out.println("Данное объявление снято с публикации");
                kafkaTemplate.send("missing-advertisements", objectMapper.writeValueAsString(new CartResult(event.getUserId(), event.getAdvertisementId(), false)));
            }

        } catch (EntityNotFoundException e) {
            System.out.println("Объявление с ID " + event.getAdvertisementId() + " не найдено.");

            kafkaTemplate.send("missing-advertisements", objectMapper.writeValueAsString(new CartResult(event.getUserId(), event.getAdvertisementId(), false)));
            
            // TODO notifyUser("Объявление не найдено.");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}