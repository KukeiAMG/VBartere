package com.vbartere.Advertisement.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Mapper.AdvertisementMapper;
import com.vbartere.Advertisement.kafka.Service.Producers.Admin.SendAdminService;
import com.vbartere.Advertisement.kafka.Service.Producers.Advertisement.MissingAdvertisementService;
import com.vbartere.Advertisement.kafka.Service.Producers.Cache.CacheAwaiterService;
import com.vbartere.Advertisement.kafka.Service.Producers.Cache.SendCacheService;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import com.vbartere.Shared.Kafka.Enum.AdvertisementEventType;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.CartResult;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
    private final MissingAdvertisementService missingAdvertisementService;

    public AdvertisementService(AdvertisementRepository advertisementRepository, SubCategoryRepository subCategoryRepository, ImageService imageService, ObjectMapper objectMapper, RedisCommands<String, String> redisCommands, CacheAwaiterService cacheAwaiterService, SendCacheService sendCacheService, SendAdminService sendAdminService, AdvertisementMapper advertisementMapper, MissingAdvertisementService missingAdvertisementService) {
        this.advertisementRepository = advertisementRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.imageService = imageService;
        this.objectMapper = objectMapper;
        this.redisCommands = redisCommands;
        this.cacheAwaiterService = cacheAwaiterService;
        this.sendCacheService = sendCacheService;
        this.sendAdminService = sendAdminService;
        this.advertisementMapper = advertisementMapper;
        this.missingAdvertisementService = missingAdvertisementService;
    }

    @Transactional(readOnly = true)
    public List<AdvertisementDTO> getAllAdvertisements() {
        String cacheKey = "advertisements:all";
        String cachedData = redisCommands.get(cacheKey);

        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData, new TypeReference<List<AdvertisementDTO>>() {});
            } catch (JsonProcessingException e) {
                System.err.println("Ошибка чтения списка из кэша: " + e.getMessage());
            }
        }

        List<Advertisement> advertisements = advertisementRepository.findAll();


        List<AdvertisementDTO> advertisementDTOs = new ArrayList<>();


        for (Advertisement advertisement : advertisements) {
            AdvertisementDTO dto = advertisementMapper.advertisementToDTO(advertisement);
            advertisementDTOs.add(dto);
        }

        return advertisementDTOs;
    }

    @Transactional(readOnly = true)
    public AdvertisementDTO getAdvertisementById(Long id) {
        String cacheKey = "advertisement:" + id;
        String cacheData = redisCommands.get(cacheKey);

        if (cacheData != null) {
            System.out.println("Объявление " + id + " взято из кэша");
            try {
                return objectMapper.readValue(cacheData, AdvertisementDTO.class);
            } catch (JsonProcessingException e) {
                System.err.println("Ошибка чтения из кэша: " + e.getMessage());
            }
        }

        // fallback на БД
        Advertisement ad = advertisementRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено в БД"));

        AdvertisementDTO dto = advertisementMapper.advertisementToDTO(ad);

        try {
            CompletableFuture<AdvertisementDTO> future = cacheAwaiterService.awaitCache(id);

            sendCacheService.sendCacheRequest(objectMapper.writeValueAsString(dto));

            return future.get(4, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Кэш недоступен или не готов: " + e.getMessage());
            return dto; // fallback
        }
    }


    @Transactional
    public Advertisement getAdvertisementEntity(Long id) {
        return advertisementRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Объявление не найдено в БД")
        );
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
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                Image image = imageService.createImage(file);
                if (image != null) {
                    image.setAdvertisement(advertisement);
                    images.add(image);
                }
            }
        }

        if (!images.isEmpty()) {
            images.get(0).setPreviewImage(true);
            advertisement.setImageList(images);
        }

        Advertisement savedAd = advertisementRepository.save(advertisement);

        AdvertisementDTO responseDTO = advertisementMapper.advertisementToDTO(savedAd);

        AdminAdvertisementDTO adminDto = advertisementMapper.toAdminDto(savedAd, AdvertisementEventType.ADVERTISEMENT_CREATED);
        sendAdminService.sendAdminRequest(objectMapper.writeValueAsString(adminDto));

        return responseDTO;
    }

    /**
     * DEPRECATED
     * <p> Этот метод используется исключительно для сдачи
     * <p> Вместо него нужно использовать: {@link AdvertisementService#updateAdvertisementFields} и 
     * {@link AdvertisementService#updateAdvertisementImages}
     */
    @Transactional
    public AdvertisementDTO updateAdvertisementById(Long advertisementID, AdvertisementDTO advertisementDTO, List<MultipartFile> files) throws IOException, ExecutionException, InterruptedException {

        Advertisement advertisement = advertisementRepository.findById(advertisementID)
                .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

        List<Image> oldImages = new ArrayList<>(advertisement.getImageList());
        for (Image img : oldImages) {
            imageService.deleteImageById(img.getId());
        }
        advertisement.getImageList().clear();

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
            List<Image> newImages = new ArrayList<>();
            for (MultipartFile file : files) {
                Image image = imageService.createImage(file);
                if (image != null) {
                    image.setAdvertisement(advertisement);
                    newImages.add(image);
                }
            }

            if (!newImages.isEmpty()) {
                newImages.getFirst().setPreviewImage(true);
                advertisement.setImageList(newImages);
            }
        }

        Advertisement savedAd = advertisementRepository.save(advertisement);

        AdvertisementDTO dto = advertisementMapper.advertisementToDTO(savedAd);
        AdminAdvertisementDTO adminAdvertisementDTO = advertisementMapper.toAdminDto(savedAd, AdvertisementEventType.ADVERTISEMENT_UPDATED);

        sendCacheService.updateCacheAsync(dto);
        sendAdminService.updateAdminAsync(adminAdvertisementDTO);

        return dto;
    }


    @Transactional
    public AdvertisementDTO updateAdvertisementFields(Long advertisementId, AdvertisementDTO advertisementDTO) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
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

        Advertisement savedAd = advertisementRepository.save(advertisement);

        AdvertisementDTO dto = advertisementMapper.advertisementToDTO(savedAd);
        AdminAdvertisementDTO adminAdvertisementDTO =
                advertisementMapper.toAdminDto(savedAd, AdvertisementEventType.ADVERTISEMENT_UPDATED);

        sendCacheService.updateCacheAsync(dto);
        sendAdminService.updateAdminAsync(adminAdvertisementDTO);

        return dto;
    }

    @Transactional
    public AdvertisementDTO updateAdvertisementImages(Long advertisementID, List<MultipartFile> files) throws IOException {

        Advertisement advertisement = advertisementRepository.findById(advertisementID)
                .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

        List<Image> oldImages = new ArrayList<>(advertisement.getImageList()); // копия, чтобы избежать ConcurrentModification
        for (Image img : oldImages) {
            imageService.deleteImageById(img.getId());
        }

        List<Image> newImages = new ArrayList<>();
        for (MultipartFile file : files) {
            Image image = imageService.createImage(file);
            if (image != null) {
                image.setAdvertisement(advertisement);
                newImages.add(image);
            }
        }

        if (!newImages.isEmpty()) {
            newImages.getFirst().setPreviewImage(true);
            advertisement.getImageList().addAll(newImages);
        }

        Advertisement savedAd = advertisementRepository.save(advertisement);

        AdvertisementDTO dto = advertisementMapper.advertisementToDTO(savedAd);
        AdminAdvertisementDTO adminAdvertisementDTO =
                advertisementMapper.toAdminDto(savedAd, AdvertisementEventType.ADVERTISEMENT_UPDATED);

        sendCacheService.updateCacheAsync(dto);
        sendAdminService.updateAdminAsync(adminAdvertisementDTO);

        return dto;
    }

    @Transactional
    public void deleteAdvertisementById(Long userId, Long advertisementID) throws JsonProcessingException {
        Advertisement advertisement = advertisementRepository.findById(advertisementID)
                .orElseThrow(() -> new EntityNotFoundException("Объявление не найдено"));

        if (!advertisement.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Это не ваше объявление");
        }
        advertisementRepository.delete(advertisement);

        AdminAdvertisementDTO adminAdvertisementDTO =
                advertisementMapper.toAdminDto(advertisement, AdvertisementEventType.ADVERTISEMENT_DELETED);

        String cacheKey = "advertisement:" + advertisementID;
        redisCommands.del(cacheKey);

        System.out.println("Объявление и его кэш успешно удалены: " + advertisementID);

        missingAdvertisementService.sendMissingAdvertisementRequest(objectMapper.writeValueAsString(
                new CartResult(userId, advertisementID, true, UserEventType.USER_REMOVE_HIS_ADVERTISEMENT)
        ));
        sendAdminService.sendAdminRequest(objectMapper.writeValueAsString(adminAdvertisementDTO));
    }
}
