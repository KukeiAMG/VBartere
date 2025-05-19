package com.vbartere.Advertisement.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Mapper.AdvertisementMapper;
import com.vbartere.Advertisement.kafka.Service.Admin.SendAdminService;
import com.vbartere.Advertisement.kafka.Service.Cache.CacheAwaiterService;
import com.vbartere.Advertisement.kafka.Service.Cache.SendCacheService;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import com.vbartere.Shared.Kafka.Enum.AdvertisementEventType;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;
    private final RedisCommands<String, String> redisCommands;
    private final CacheAwaiterService cacheAwaiterService;
    private final SendCacheService sendCacheService;
    private final SendAdminService sendAdminService;
    private final AdvertisementMapper advertisementMapper;

    public AdvertisementService(AdvertisementRepository advertisementRepository, SubCategoryRepository subCategoryRepository, ImageService imageService, ObjectMapper objectMapper, RedisCommands<String, String> redisCommands, CacheAwaiterService cacheAwaiterService, SendCacheService sendCacheService, SendAdminService sendAdminService, AdvertisementMapper advertisementMapper) {
        this.advertisementRepository = advertisementRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.imageService = imageService;
        this.objectMapper = objectMapper;
        this.redisCommands = redisCommands;
        this.cacheAwaiterService = cacheAwaiterService;
        this.sendCacheService = sendCacheService;
        this.sendAdminService = sendAdminService;
        this.advertisementMapper = advertisementMapper;
    }
//    public AdvertisementDTO toDTO(Advertisement advertisement) {
//        AdvertisementDTO dto = new AdvertisementDTO();
//        dto.setId(advertisement.getId());
//        dto.setTitle(advertisement.getTitle());
//        dto.setDescription(advertisement.getDescription());
//        dto.setOwnerId(advertisement.getOwnerId());
//        dto.setBuyersId(advertisement.getBuyersId());
//        dto.setStatus(advertisement.getStatus());
//
//        if (advertisement.getStatus() != null) {
//            dto.setStatus(advertisement.getStatus());
//        } else {
//            dto.setStatus(false); // Значение по умолчанию, если status = null
//        }
//
//        if (advertisement.getSubcategory() != null) {
//            dto.setSubCategoryId(advertisement.getSubcategory().getId());
//        }
//
//        if (advertisement.getImageList() != null && !advertisement.getImageList().isEmpty()) {
//            List<Long> imageIds = new ArrayList<>();
//            for (Image image : advertisement.getImageList()) {
//                imageIds.add(image.getId());
//            }
//            dto.setImagesId(imageIds);
//        } else {
//            dto.setImagesId(Collections.emptyList());
//        }
//
//        return dto;
//    }

    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AdvertisementDTO getAdvertisementById(Long id) throws JsonProcessingException, ExecutionException, InterruptedException {

        String cacheKey = "advertisement:" + id;
        String cacheData = redisCommands.get(cacheKey);

        if (cacheData != null) {
            System.out.println("Объявление " + id + " взято из кэша");
            return objectMapper.readValue(cacheData, AdvertisementDTO.class);
        } else {
            Advertisement advertisement = advertisementRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Объявления нет в БД"));

            AdvertisementDTO advertisementDTO = advertisementMapper.advertisementToDTO(advertisement);

            // Асинхронно отправляем в Kafka, не блокируя поток
            sendCacheService.sendCacheRequest(objectMapper.writeValueAsString(advertisementDTO));

            CompletableFuture<Advertisement> future = cacheAwaiterService.awaitCache(advertisement.getId());

            AdvertisementDTO resultDto;
            try {
                Advertisement advFromCache = future.get(10, TimeUnit.SECONDS);
                resultDto = advertisementMapper.advertisementToDTO(advFromCache);
            } catch (TimeoutException e) {
                System.err.println("Кэш ещё не готов: " + e.getMessage());
                resultDto = advertisementDTO; // возвращаем DTO из базы
            } catch (Exception e) {
                System.err.println("Ошибка ожидания кэша: " + e.getMessage());
                resultDto = advertisementDTO;
            }

            return resultDto;
        }
    }

    @Transactional
    public AdvertisementDTO createAdvertisement(AdvertisementDTO advertisementDTO, List<MultipartFile> files, Long userId) throws IOException {
        SubCategory subCategory = subCategoryRepository.findById(advertisementDTO.getSubCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Подкатегория не найдена"));

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(advertisementDTO.getTitle());
        advertisement.setDescription(advertisementDTO.getDescription());
        advertisement.setSubcategory(subCategory);
        advertisement.setStatus(advertisementDTO.isStatus());
        advertisement.setOwnerId(userId);

        List<Image> images = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Image image = imageService.createImage(file);
            image.setAdvertisement(advertisement);
            images.add(image);
        }

        if (!images.isEmpty()) {
            images.get(0).setPreviewImage(true);
            advertisement.setImageList(images);
        }

        Advertisement savedAd = advertisementRepository.save(advertisement);

//        AdvertisementDTO responseDTO = new AdvertisementDTO();
//        responseDTO.setId(savedAd.getId());
//        responseDTO.setTitle(savedAd.getTitle());
//        responseDTO.setDescription(savedAd.getDescription());
//        responseDTO.setOwnerId(savedAd.getOwnerId());
//        responseDTO.setBuyersId(savedAd.getBuyersId());
//        responseDTO.setStatus(savedAd.getStatus());
//
//        if (savedAd.getSubcategory() != null) {
//            responseDTO.setSubCategoryId(savedAd.getSubcategory().getId());
//        }
//
//        List<Long> imageIds = new ArrayList<>();
//        List<Image> savedImages = savedAd.getImageList();
//        for (int i = 0; i < savedImages.size(); i++) {
//            imageIds.add(savedImages.get(i).getId());
//        }
//
//        responseDTO.setImagesId(imageIds);
        AdvertisementDTO responseDTO = advertisementMapper.advertisementToDTO(savedAd);

//        AdminAdvertisementDTO adminAdvertisementDTO = new AdminAdvertisementDTO();
//        adminAdvertisementDTO.setId(savedAd.getId());
//        adminAdvertisementDTO.setTitle(savedAd.getTitle());
//        adminAdvertisementDTO.setDescription(savedAd.getDescription());
//        adminAdvertisementDTO.setOwnerId(savedAd.getOwnerId());
//        adminAdvertisementDTO.setBuyersId(savedAd.getBuyersId());
//        adminAdvertisementDTO.setStatus(savedAd.getStatus());
//        adminAdvertisementDTO.setSubcategoryId(savedAd.getSubcategory().getId());
//        adminAdvertisementDTO.setSubcategoryTitle(savedAd.getSubcategory().getName());
//        adminAdvertisementDTO.setEventType(AdvertisementEventType.ADVERTISEMENT_CREATED);

        AdminAdvertisementDTO adminDto = advertisementMapper.toAdminDto(savedAd, AdvertisementEventType.ADVERTISEMENT_CREATED);
        sendAdminService.sendAdminRequest(objectMapper.writeValueAsString(adminDto));

        return responseDTO;
    }

    @Transactional
    public AdvertisementDTO updateAdvertisementById(Long advertisementID, AdvertisementDTO advertisementDTO, List<MultipartFile> files) throws IOException, ExecutionException, InterruptedException {

        Advertisement advertisement = advertisementRepository.findById(advertisementID)
                .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

        if (advertisementDTO.getSubCategoryId() != null) {
            SubCategory subCategory = subCategoryRepository.findById(advertisementDTO.getSubCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Подкатегория не найдена"));
            advertisement.setSubcategory(subCategory);
        }

        if (advertisementDTO.getTitle() != null) {
            advertisement.setTitle(advertisementDTO.getTitle());
        }

        if (advertisementDTO.getDescription() != null) {
            advertisement.setDescription(advertisementDTO.getDescription());
        }

        if (advertisementDTO.getOwnerId() != null) {
            advertisement.setOwnerId(advertisementDTO.getOwnerId());
        }

        if (files != null && !files.isEmpty()) {
            List<Image> images = new ArrayList<>();
            for (MultipartFile file : files) {
                Image image = imageService.createImage(file);
                image.setAdvertisement(advertisement);
                images.add(image);
            }
            images.get(0).setPreviewImage(true);
            advertisement.setImageList(images);
        }

        Advertisement savedAd = advertisementRepository.save(advertisement);

        AdvertisementDTO dto = advertisementMapper.advertisementToDTO(savedAd);

        AdminAdvertisementDTO adminAdvertisementDTO = advertisementMapper.toAdminDto(savedAd, AdvertisementEventType.ADVERTISEMENT_UPDATED);

        sendCacheService.updateCacheAsync(dto);
        sendAdminService.updateAdminAsync(adminAdvertisementDTO);

        return dto;
    }

    @Transactional
    public void deleteAdvertisementById(Long advertisementID) throws JsonProcessingException {
        if (advertisementRepository.existsById(advertisementID)) {
            advertisementRepository.deleteById(advertisementID);

            AdminAdvertisementDTO adminAdvertisementDTO = new AdminAdvertisementDTO();
            adminAdvertisementDTO.setId(advertisementID);
            adminAdvertisementDTO.setEventType(AdvertisementEventType.ADVERTISEMENT_DELETED);

            String cacheKey = "advertisement:" + advertisementID;
            redisCommands.del(cacheKey);

            System.out.println("Объявление и его кэш успешно удалены: " + advertisementID);

            sendAdminService.sendAdminRequest(objectMapper.writeValueAsString(adminAdvertisementDTO));
        } else {
            throw new EntityNotFoundException("Объявление не найдено");
        }
    }
}
