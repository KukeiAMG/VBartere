package com.vbartere.AdminService.Kafka.Service.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.AdminService.Model.AdminAdvertisement;
import com.vbartere.AdminService.Model.AdminUser;
import com.vbartere.AdminService.Repository.AdminAdvertisementRepository;
import com.vbartere.AdminService.Repository.AdminUserRepository;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminAdvertisementEventConsumer {

    private final AdminAdvertisementRepository adminAdvertisementRepository;
    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    public AdminAdvertisementEventConsumer(AdminAdvertisementRepository adminAdvertisementRepository, AdminUserRepository adminUserRepository, ObjectMapper objectMapper) {
        this.adminAdvertisementRepository = adminAdvertisementRepository;
        this.adminUserRepository = adminUserRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "administration.advertisement.event")
    public void handleAdvertisementEvent(String message) throws JsonProcessingException {
        AdminAdvertisementDTO event = objectMapper.readValue(message, AdminAdvertisementDTO.class);

        System.out.println(event.toString());

        if (event.getId() == null) {
            System.err.println("Получен объект advertisement с пустым ID: " + event);
            return; // или выбросить исключение, если это критично
        }

        switch (event.getEventType()) {
            case ADVERTISEMENT_CREATED, ADVERTISEMENT_UPDATED -> {
                if (event.getBuyersId() != null) {
                    AdminUser buyer = adminUserRepository.findById(event.getBuyersId())
                            .orElseThrow(() -> new EntityNotFoundException("Покупатель не найден"));
                    event.setBuyersId(event.getBuyersId());
                    event.setBuyerUsername(buyer.getName());
                } else {
                    event.setBuyersId(null);
                    event.setBuyerUsername(null);
                }
                Optional<AdminAdvertisement> optionalAdvertisement = adminAdvertisementRepository.findById(event.getId());

                AdminAdvertisement adminAdvertisement = optionalAdvertisement.orElseGet(() -> {
                    AdminAdvertisement newAdvertisement = new AdminAdvertisement();
                    newAdvertisement.setId(event.getId());
                    return newAdvertisement;
                });

                AdminUser owner = adminUserRepository.findById(event.getOwnerId())
                        .orElseThrow(() -> new EntityNotFoundException("Владелец не найден"));

                adminAdvertisement.setTitle(event.getTitle());
                adminAdvertisement.setDescription(event.getDescription());
                adminAdvertisement.setSubcategoryId(event.getSubcategoryId());
                adminAdvertisement.setSubcategoryTitle(event.getSubcategoryTitle());
                adminAdvertisement.setOwnerId(event.getOwnerId());
                adminAdvertisement.setOwnerUsername(owner.getName());
                adminAdvertisement.setStatus(event.getStatus());
                adminAdvertisement.setBuyersId(event.getBuyersId());

                if (event.getBuyersId() != null) {
                    adminAdvertisement.setBuyersId(event.getBuyersId());

                    AdminUser buyer = adminUserRepository.findById(event.getBuyersId())
                            .orElseThrow(() -> new EntityNotFoundException("Покупатель не найден"));

                    adminAdvertisement.setBuyerUsername(buyer.getName());
                } else {
                    adminAdvertisement.setBuyersId(null);
                    adminAdvertisement.setBuyerUsername(null);
                }

                adminAdvertisementRepository.save(adminAdvertisement);
            }

            case ADVERTISEMENT_DELETED -> {
                adminAdvertisementRepository.deleteById(event.getId());
            }
        }
    }
}
