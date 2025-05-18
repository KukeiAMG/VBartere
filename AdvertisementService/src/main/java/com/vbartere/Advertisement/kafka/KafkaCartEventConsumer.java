package com.vbartere.Advertisement.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Advertisement.kafka.Service.Advertisement.MissingAdvertisementService;
import com.vbartere.Advertisement.kafka.Service.Cache.SendCacheService;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import com.vbartere.Shared.Kafka.Events.CartEvent;
import com.vbartere.Shared.Kafka.Events.CartResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class KafkaCartEventConsumer {

    private final AdvertisementService advertisementService;
    private final AdvertisementRepository advertisementRepository;
    private final ObjectMapper objectMapper;
    private final SendCacheService sendCacheService;
    private final MissingAdvertisementService missingAdvertisementService;

    public KafkaCartEventConsumer(AdvertisementService advertisementService, AdvertisementRepository advertisementRepository, ObjectMapper objectMapper, SendCacheService sendCacheService, MissingAdvertisementService missingAdvertisementService) {
        this.advertisementService = advertisementService;
        this.advertisementRepository = advertisementRepository;
        this.objectMapper = objectMapper;
        this.sendCacheService = sendCacheService;
        this.missingAdvertisementService = missingAdvertisementService;
    }


    @KafkaListener(topics = "cart-events", groupId = "advertisement-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleCartEvent(String message) throws JsonProcessingException {
        CartEvent event = objectMapper.readValue(message, CartEvent.class);
        try {
            AdvertisementDTO cachedAdvertisement = advertisementService.getAdvertisementById(event.getAdvertisementId());

            if (cachedAdvertisement.isStatus()) {
                System.out.println("Объявление доступно");

                // Получаем managed-сущность из БД
                Advertisement persistentAd = advertisementRepository.findById(event.getAdvertisementId())
                        .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

                persistentAd.setBuyersId(event.getUserId());
                persistentAd.setStatus(false);
                advertisementRepository.save(persistentAd);

                sendCacheService.sendCacheRequest(objectMapper.writeValueAsString(persistentAd));

                System.out.println("\nОбработка события: " + event + "\nТовар: " + persistentAd.getId());
                missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(new CartResult(event.getUserId(), event.getAdvertisementId(), true)));
            }
            else {
                System.out.println("Данное объявление снято с публикации");
                missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(new CartResult(event.getUserId(), event.getAdvertisementId(), false)));
            }

        } catch (EntityNotFoundException e) {
            System.out.println("Объявление с ID " + event.getAdvertisementId() + " не найдено.");

            missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(new CartResult(event.getUserId(), event.getAdvertisementId(), false)));
            
            // TODO notifyUser("Объявление не найдено.");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}