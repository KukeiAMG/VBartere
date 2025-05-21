package com.vbartere.Advertisement.kafka.Service.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Mapper.AdvertisementMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Advertisement.kafka.Service.Producers.Advertisement.MissingAdvertisementService;
import com.vbartere.Advertisement.kafka.Service.Producers.Cache.SendCacheService;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import com.vbartere.Shared.Kafka.Events.CartEvent;
import com.vbartere.Shared.Kafka.Events.CartResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class KafkaCartEventConsumer {

    private final AdvertisementService advertisementService;
    private final AdvertisementRepository advertisementRepository;
    private final ObjectMapper objectMapper;
    private final SendCacheService sendCacheService;
    private final MissingAdvertisementService missingAdvertisementService;
    private final AdvertisementMapper advertisementMapper;

    public KafkaCartEventConsumer(AdvertisementService advertisementService, AdvertisementRepository advertisementRepository, ObjectMapper objectMapper, SendCacheService sendCacheService, MissingAdvertisementService missingAdvertisementService, AdvertisementMapper advertisementMapper) {
        this.advertisementService = advertisementService;
        this.advertisementRepository = advertisementRepository;
        this.objectMapper = objectMapper;
        this.sendCacheService = sendCacheService;
        this.missingAdvertisementService = missingAdvertisementService;
        this.advertisementMapper = advertisementMapper;
    }


    @KafkaListener(topics = "cart.events")
    public void handleCartEvent(String message) throws JsonProcessingException {
        CartEvent event = objectMapper.readValue(message, CartEvent.class);

        if (event.isBanned()) {
            System.out.println("Пользователь " + event.getUserId() + " заблокирован и не может добавить объявление " + event.getAdvertisementId());

            missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                    new CartResult(event.getUserId(), event.getAdvertisementId(), false)
            ));

            return;
        }

        try {
            AdvertisementDTO cachedAdvertisement;

            try {
                cachedAdvertisement = advertisementService.getAdvertisementById(event.getAdvertisementId());

                if (event.getUserId().equals(cachedAdvertisement.getOwnerId())) {
                    System.out.println("Нельзя добавить свое же объявление");

                    missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                            new CartResult(event.getUserId(), event.getAdvertisementId(), false)
                    ));

                    return;
                }

            } catch (Exception e) {
                // Если кэш недоступен, fallback на БД
                System.out.println("Кэш недоступен, пробуем из БД: " + e.getMessage());

                Advertisement persistentAd = advertisementRepository.findByIdWithImages(event.getAdvertisementId())
                        .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено в БД"));

                cachedAdvertisement = advertisementMapper.advertisementToDTO(persistentAd);
            }

            if (cachedAdvertisement.isStatus()) {
                System.out.println("\nОбъявление доступно");

                Advertisement persistentAd = advertisementRepository.findByIdWithImages(event.getAdvertisementId())
                        .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

                persistentAd.setBuyersId(event.getUserId());
                persistentAd.setStatus(false);
                advertisementRepository.save(persistentAd);

                AdvertisementDTO updatedDto = advertisementMapper.advertisementToDTO(persistentAd);
                sendCacheService.updateCacheAsync(updatedDto);

                System.out.println("\nОбработка события: " + event + "\nТовар: " + persistentAd.getId());

                missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                        new CartResult(event.getUserId(), event.getAdvertisementId(), true)
                ));
            } else {
                System.out.println("Данное объявление снято с публикации");
                missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                        new CartResult(event.getUserId(), event.getAdvertisementId(), false)
                ));
            }

        } catch (EntityNotFoundException e) {
            System.out.println("Объявление с ID " + event.getAdvertisementId() + " не найдено.");
            missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                    new CartResult(event.getUserId(), event.getAdvertisementId(), false)
            ));
        } catch (Exception e) {
            System.out.println("Ошибка обработки события: " + e.getMessage());
        }
    }
}