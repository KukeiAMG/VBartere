package com.vbartere.Advertisement.kafka.Service.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Mapper.AdvertisementMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Advertisement.kafka.Service.Producers.Admin.SendAdminService;
import com.vbartere.Advertisement.kafka.Service.Producers.Advertisement.MissingAdvertisementService;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import com.vbartere.Shared.Kafka.Enum.AdvertisementEventType;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.CartResult;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAdvertisementEventConsumer {

    private final ObjectMapper objectMapper;
    private final AdvertisementRepository advertisementRepository;
    private final MissingAdvertisementService missingAdvertisementService;
    private final SendAdminService sendAdminService;
    private final AdvertisementMapper advertisementMapper;
    private final AdvertisementService advertisementService;

    public UserAdvertisementEventConsumer(ObjectMapper objectMapper, AdvertisementRepository advertisementRepository, MissingAdvertisementService missingAdvertisementService, SendAdminService sendAdminService, AdvertisementMapper advertisementMapper, AdvertisementService advertisementService) {
        this.objectMapper = objectMapper;
        this.advertisementRepository = advertisementRepository;
        this.missingAdvertisementService = missingAdvertisementService;
        this.sendAdminService = sendAdminService;
        this.advertisementMapper = advertisementMapper;
        this.advertisementService = advertisementService;
    }

    @KafkaListener(topics = "user.advertisement.events")
    public void handleUserEvent(String message) throws JsonProcessingException {
        UserEvent userEvent = objectMapper.readValue(message, UserEvent.class);

        if (userEvent.getEvent() == UserEventType.USER_DELETED) {
            List<Advertisement> advertisementList =
                    advertisementRepository.findAdvertisementsByOwnerId(userEvent.getId());

            for (Advertisement advertisement : advertisementList) {
                advertisementService.deleteAdvertisementById(userEvent.getId(), advertisement.getId());

                CartResult cartResult = new CartResult(
                        userEvent.getId(),
                        advertisement.getId(),
                        true,
                        UserEventType.USER_REMOVE_HIS_ADVERTISEMENT
                );
                missingAdvertisementService.sendMissingAdvertisementRequest(
                        objectMapper.writeValueAsString(cartResult)
                );

                AdminAdvertisementDTO adminAdvertisementDTO = advertisementMapper.toAdminDto(
                        advertisement,
                        AdvertisementEventType.ADVERTISEMENT_DELETED
                );
                sendAdminService.sendAdminRequest(objectMapper.writeValueAsString(adminAdvertisementDTO));
            }

            System.out.println("Все объявления пользователя " + userEvent.getId() + " успешно удалены");
        }
    }
}
