package com.vbartere.Advertisement.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.kafka.Service.CacheAwaiterService;
import com.vbartere.Advertisement.kafka.Service.SendCacheService;
import com.vbartere.Shared.Kafka.DTO.AdvertisementDTO;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;
    private final RedisCommands<String, String> redisCommands;
    private final CacheAwaiterService cacheAwaiterService;
    private final SendCacheService sendCacheService;

    public AdvertisementService(AdvertisementRepository advertisementRepository, SubCategoryRepository subCategoryRepository, ImageService imageService, ObjectMapper objectMapper, RedisCommands<String, String> redisCommands, CacheAwaiterService cacheAwaiterService, SendCacheService sendCacheService) {
        this.advertisementRepository = advertisementRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.imageService = imageService;
        this.objectMapper = objectMapper;
        this.redisCommands = redisCommands;
        this.cacheAwaiterService = cacheAwaiterService;
        this.sendCacheService = sendCacheService;
    }

    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Advertisement getAdvertisementById(Long id) throws JsonProcessingException, ExecutionException, InterruptedException {

        String cacheKey = "advertisement:" + id;
        String cacheData = redisCommands.get(cacheKey);

        if (cacheData != null) {
            System.out.println("Объявление " + cacheData + " взято из кэша");
            return objectMapper.readValue(cacheData, Advertisement.class);
        } else {

            Advertisement advertisement = advertisementRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Объявления нет в БД"));

            AdvertisementDTO advertisementDTO = objectMapper.convertValue(advertisement, AdvertisementDTO.class);

            // Преобразуем сущность в DTO с использованием идентификаторов изображений
            List<Long> imageIds = advertisement.getImageList().stream()
                    .map(Image::getId)
                    .collect(Collectors.toList());
            advertisementDTO.setImagesId(imageIds);

            if (advertisement.getSubcategory() != null) {
                advertisementDTO.setSubCategoryId(advertisement.getSubcategory().getId());
            }

            sendCacheService.sendCacheRequest(objectMapper.writeValueAsString(advertisementDTO));

            // Ожидаем появления кэша красиво
            return cacheAwaiterService.awaitCache(advertisement.getId())
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(throwable -> {
                        throw new RuntimeException("Кэш ещё не готов. Попробуйте позже.");
                    })
                    .get(); // блокируем поток до получения результата
        }
    }

    @Transactional
    public Advertisement createAdvertisement(AdvertisementDTO advertisementDTO, List<MultipartFile> files, Long userId) throws IOException {
        SubCategory subCategory = subCategoryRepository.findById(advertisementDTO.getSubCategoryId())
                .orElseThrow(() -> new RuntimeException("Подкатегория не найдена"));

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(advertisementDTO.getTitle());
        advertisement.setDescription(advertisementDTO.getDescription());
        advertisement.setSubcategory(subCategory);
        advertisement.setStatus(advertisementDTO.isStatus());
        advertisement.setOwnerId(userId);

        List<Image> images = new ArrayList<>();
        for(MultipartFile file : files) {
            Image image = imageService.createImage(file);
            image.setAdvertisement(advertisement);
            images.add(image);
        }

        if (!images.isEmpty()) {
            images.getFirst().setPreviewImage(true);
            advertisement.setImageList(images);
        }

        advertisement.setImageList(images);

        return advertisementRepository.save(advertisement);
    }

    @Transactional
    public Advertisement updateAdvertisementById(Long advertisementID, AdvertisementDTO advertisementDTO, List<MultipartFile> files) throws IOException {

        Advertisement advertisement = advertisementRepository.findById(advertisementID)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        if (advertisementDTO.getSubCategoryId() != null) {
            SubCategory subCategory = subCategoryRepository.findById(advertisementDTO.getSubCategoryId())
                    .orElseThrow(() -> new RuntimeException("Подкатегория не найдена"));
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
            images.getFirst().setPreviewImage(true);
            advertisement.setImageList(images);
        }

        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);

        sendCacheService.sendCacheRequest(objectMapper.writeValueAsString(savedAdvertisement));

        return savedAdvertisement;
    }

    @Transactional
    public void deleteAdvertisementById(Long advertisementID) {
        advertisementRepository.deleteById(advertisementID);

        String cacheKey = "advertisement:" + advertisementID;
        redisCommands.del(cacheKey);

        System.out.println("Объявление и его кэш успешно удалены: " + advertisementID);
    }
}
