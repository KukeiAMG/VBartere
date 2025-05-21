package com.vbartere.Advertisement.kafka.Service.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Mapper.AdvertisementMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Service.AdvertisementService;
import com.vbartere.Advertisement.kafka.Service.Producers.Admin.SendAdminService;
import com.vbartere.Advertisement.kafka.Service.Producers.Advertisement.MissingAdvertisementService;
import com.vbartere.Advertisement.kafka.Service.Producers.Cache.SendCacheService;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import com.vbartere.Shared.Kafka.Enum.AdvertisementEventType;
import com.vbartere.Shared.Kafka.Enum.CartEventType;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.CartEvent;
import com.vbartere.Shared.Kafka.Events.CartResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class KafkaCartEventConsumer {

    private final AdvertisementService advertisementService;
    private final AdvertisementRepository advertisementRepository;
    private final ObjectMapper objectMapper;
    private final SendCacheService sendCacheService;
    private final MissingAdvertisementService missingAdvertisementService;
    private final AdvertisementMapper advertisementMapper;
    private final SendAdminService sendAdminService;

    public KafkaCartEventConsumer(AdvertisementService advertisementService, AdvertisementRepository advertisementRepository, ObjectMapper objectMapper, SendCacheService sendCacheService, MissingAdvertisementService missingAdvertisementService, AdvertisementMapper advertisementMapper, SendAdminService sendAdminService) {
        this.advertisementService = advertisementService;
        this.advertisementRepository = advertisementRepository;
        this.objectMapper = objectMapper;
        this.sendCacheService = sendCacheService;
        this.missingAdvertisementService = missingAdvertisementService;
        this.advertisementMapper = advertisementMapper;
        this.sendAdminService = sendAdminService;
    }

    @KafkaListener(topics = "cart.events")
    public void handleCartEvent(String message) throws JsonProcessingException {
        CartEvent event = objectMapper.readValue(message, CartEvent.class);

        switch (event.getCartEventType()){
            case ADD_ADVERTISEMENT_TO_CART -> {

                if (event.isBanned()) {
                    System.out.println("Пользователь " + event.getUserId() + " заблокирован");

                    missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                            new CartResult(event.getUserId(), event.getAdvertisementId(), false, UserEventType.USER_BANNED)
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
                                    new CartResult(event.getUserId(), event.getAdvertisementId(), false, UserEventType.USER_ADD_ADVERTISEMENT_TO_CART)
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

                        AdminAdvertisementDTO adminAdvertisementDTO = advertisementMapper.toAdminDto(persistentAd, AdvertisementEventType.ADVERTISEMENT_UPDATED);

                        System.out.println("\nОбработка события: " + event + "\nТовар: " + persistentAd.getId());

                        missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                                new CartResult(event.getUserId(), event.getAdvertisementId(), true, UserEventType.USER_ADD_ADVERTISEMENT_TO_CART)
                        ));
                        sendAdminService.sendAdminRequest(objectMapper.writeValueAsString(adminAdvertisementDTO));
                    } else {
                        System.out.println("Данное объявление снято с публикации");
                        missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                                new CartResult(event.getUserId(), event.getAdvertisementId(), false, UserEventType.USER_ADD_ADVERTISEMENT_TO_CART)
                        ));
                    }

                } catch (EntityNotFoundException e) {
                    System.out.println("Объявление с ID " + event.getAdvertisementId() + " не найдено.");
                    missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                            new CartResult(event.getUserId(), event.getAdvertisementId(), false, UserEventType.USER_DID_NOT_FIND_THE_ADVERTISEMENT)
                    ));
                } catch (Exception e) {
                    System.out.println("Ошибка обработки события: " + e.getMessage());
                }
            }

            case REMOVE_ADVERTISEMENT_FROM_CART -> {
                try {
                    Advertisement advertisement = advertisementRepository.findByIdWithImages(event.getAdvertisementId())
                            .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

                    advertisement.setBuyersId(null);
                    advertisement.setStatus(true);
                    advertisementRepository.save(advertisement);

                    AdvertisementDTO advertisementDTO = advertisementMapper.advertisementToDTO(advertisement);

                    AdminAdvertisementDTO adminAdvertisementDTO = advertisementMapper.toAdminDto(advertisement, AdvertisementEventType.ADVERTISEMENT_UPDATED);

                    sendCacheService.updateCacheAsync(advertisementDTO);
                    sendAdminService.sendAdminRequest(objectMapper.writeValueAsString(adminAdvertisementDTO));

                    missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                            new CartResult(event.getUserId(), event.getAdvertisementId(), true, UserEventType.USER_REMOVE_ADVERTISEMENT_FROM_CART)
                    ));
                } catch (EntityNotFoundException e) {
                    System.out.println("Объявление не найдено: " + event.getAdvertisementId());
                    missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                            new CartResult(event.getUserId(), event.getAdvertisementId(), false, UserEventType.USER_DID_NOT_FIND_THE_ADVERTISEMENT)
                    ));
                } catch (Exception e) {
                    System.err.println("Ошибка при удалении из корзины: " + e.getMessage());
                }
            }

            case CLEAR_CART -> {
                try {
                    Long userId = event.getUserId();
                    System.out.println("Очистка корзины для пользователя: " + userId);

                    List<Advertisement> reservedAds = advertisementRepository.findByBuyersIdAndStatusFalseWithImages(userId);

                    System.out.println("Найдено объявлений для очистки: " + reservedAds.size());

                    for (int i = 0; i < reservedAds.size(); i++) {
                        Advertisement advertisement = reservedAds.get(i);

                        advertisement.setBuyersId(null);
                        advertisement.setStatus(true);
                    }

                    advertisementRepository.saveAll(reservedAds);

                    for (int i = 0; i < reservedAds.size(); i++) {
                        Advertisement advertisement = reservedAds.get(i);

                        AdvertisementDTO advertisementDTO = advertisementMapper.advertisementToDTO(advertisement);
                        sendCacheService.updateCacheAsync(advertisementDTO);

                        AdminAdvertisementDTO adminAdvertisementDTO = advertisementMapper.toAdminDto(advertisement, AdvertisementEventType.ADVERTISEMENT_UPDATED);
                        sendAdminService.updateAdminAsync(adminAdvertisementDTO);
                    }

                    // Уведомление в Kafka
                    CartResult result = new CartResult(userId, null, true, UserEventType.USER_CLEARED_HIS_CART);
                    String json = objectMapper.writeValueAsString(result);
                    missingAdvertisementService.sendMissingAdvertisementRequest(json);

                    System.out.println("Корзина очищена для пользователя: " + userId);
                } catch (Exception e) {
                    System.out.println("Ошибка при очистке корзины: " + e.getMessage());
                }
            }
        }
    }
}