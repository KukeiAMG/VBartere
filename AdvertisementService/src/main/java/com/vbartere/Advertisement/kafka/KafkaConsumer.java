package com.vbartere.Advertisement.kafka;

import com.vbartere.Advertisement.DTO.AdvertisementDTO;
import com.vbartere.Advertisement.DTO.AdvertisementMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Service.AdvertisementService;

import com.vbartere.Shared.Kafka.CartEvent;
import com.vbartere.Shared.Kafka.CartResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private final AdvertisementService advertisementService;
    private final AdvertisementRepository advertisementRepository;
    private final KafkaTemplate<String, CartResult> kafkaTemplate;

    public KafkaConsumer(AdvertisementService advertisementService, AdvertisementRepository advertisementRepository, KafkaTemplate<String, CartResult> kafkaTemplate) {
        this.advertisementService = advertisementService;
        this.advertisementRepository = advertisementRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "cart-events", groupId = "advertisement-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleCartEvent(CartEvent event) {
        try {
            AdvertisementDTO advertisementDTO = advertisementService.getAdvertisementById(event.getAdvertisementId());

            if (advertisementDTO.isStatus() == true) {
                advertisementDTO.setBuyersId(event.getUserId());
                advertisementDTO.setStatus(false);

                Advertisement existingEntity = advertisementService.findById(event.getAdvertisementId());
                Advertisement updatedEntity = AdvertisementMapper.toEntity(advertisementDTO, existingEntity);
                advertisementRepository.save(updatedEntity);

                System.out.println("\nОбработка события: " + event + "\nТовар: " + advertisementDTO);
                kafkaTemplate.send("missing-advertisements", new CartResult(event.getUserId(), event.getAdvertisementId(), true));
            }
            else {
                System.out.println("Данное объявление снято с публикации");
                kafkaTemplate.send("missing-advertisements", new CartResult(event.getUserId(), event.getAdvertisementId(), false));
            }

        } catch (EntityNotFoundException e) {
            // Обработка случая, когда объявление не найдено
            System.out.println("Объявление с ID " + event.getAdvertisementId() + " не найдено.");

            String message = "Объявление с ID " + event.getAdvertisementId() + " не найдено.";
            kafkaTemplate.send("missing-advertisements", new CartResult(event.getUserId(), event.getAdvertisementId(), false));
            //kafkaTemplate.send("missing-advertisements", message);

            // TODO notifyUser("Объявление не найдено.");
        }
    }
}