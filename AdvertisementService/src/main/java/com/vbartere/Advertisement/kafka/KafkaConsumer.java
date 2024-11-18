package com.vbartere.Advertisement.kafka;

import com.vbartere.Advertisement.DTO.AdvertisementDTO;
import com.vbartere.Advertisement.DTO.AdvertisementMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Shared.Kafka.CartEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private final AdvertisementService advertisementService;
    private final AdvertisementRepository advertisementRepository;

    public KafkaConsumer(AdvertisementService advertisementService, AdvertisementRepository advertisementRepository) {
        this.advertisementService = advertisementService;
        this.advertisementRepository = advertisementRepository;
    }

    @KafkaListener(topics = "cart-events", groupId = "advertisement-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleCartEvent(CartEvent event) {
        AdvertisementDTO advertisementDTO = advertisementService.getAdvertisementById(event.getAdvertisementId());
        advertisementDTO.setBuyersId(event.getUserId());
        advertisementDTO.setStatus(false);

        Advertisement existingEntity = advertisementService.findById(event.getAdvertisementId());
        Advertisement updatedEntity = AdvertisementMapper.toEntity(advertisementDTO, existingEntity);
        advertisementRepository.save(updatedEntity);

        System.out.println("\nОбработка события: " + event + "\nТовар: " + advertisementDTO);
    }
}