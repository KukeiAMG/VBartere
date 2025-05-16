package com.vbartere.Advertisement.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import com.vbartere.Advertisement.kafka.Service.CacheAwaiterService;
import com.vbartere.Advertisement.kafka.Service.SendCacheService;
import com.vbartere.Shared.Kafka.DTO.Advertisement.AdvertisementDTO;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vbartere.Advertisement.Model.Image;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdvertisementServiceTest {

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private RedisCommands<String, String> redisCommands;

    @Mock
    private CacheAwaiterService cacheAwaiterService;

    @Mock
    private SendCacheService sendCacheService;

    @InjectMocks
    private AdvertisementService advertisementService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // Вручную устанавливаем objectMapper в сервис
        advertisementService = new AdvertisementService(
                advertisementRepository,
                subCategoryRepository,
                imageService,
                objectMapper, // Передаем реальный ObjectMapper
                redisCommands,
                cacheAwaiterService,
                sendCacheService
        );
    }

    // Тест для getAllAdvertisements()
    @Test
    void getAllAdvertisements_ReturnsList() {
        // Arrange
        List<Advertisement> mockAds = List.of(new Advertisement(), new Advertisement());
        when(advertisementRepository.findAll()).thenReturn(mockAds);

        // Act
        List<Advertisement> result = advertisementService.getAllAdvertisements();

        // Assert
        assertEquals(2, result.size());
        verify(advertisementRepository, times(1)).findAll();
    }

    // Тест для getAdvertisementById() (кэш есть)
    @Test
    void getAdvertisementById_WhenCacheExists_ReturnsCachedDTO() throws Exception {
        // Arrange
        Long adId = 1L;
        AdvertisementDTO cachedDTO = new AdvertisementDTO();
        cachedDTO.setId(adId);

        String cachedJson = objectMapper.writeValueAsString(cachedDTO);

        // Настраиваем мок Redis
        when(redisCommands.get("advertisement:" + adId)).thenReturn(cachedJson);

        // Act
        AdvertisementDTO result = advertisementService.getAdvertisementById(adId);

        // Assert
        assertEquals(adId, result.getId(), "ID объявления должно совпадать с кэшированным DTO");
        verify(advertisementRepository, never()).findById(any());
        verify(redisCommands, times(1)).get("advertisement:" + adId);
    }

    // Тест для getAdvertisementById() (кэша нет)
    @Test
    void getAdvertisementById_WhenCacheMissing_ReturnsFromDB() throws Exception {
        // Arrange
        Long adId = 1L;
        Advertisement ad = new Advertisement();
        ad.setId(adId);
        ad.setStatus(false);

        // Создаем реальный DTO через метод сервиса
        AdvertisementDTO realDTO = advertisementService.toDTO(ad);

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(redisCommands.get(anyString())).thenReturn(null);

        // Используем any() вместо конкретного объекта
        when(objectMapper.writeValueAsString(any(AdvertisementDTO.class)))
                .thenReturn("{\"id\":1,\"status\":false}");

        CompletableFuture<Advertisement> mockFuture = CompletableFuture.completedFuture(ad);
        when(cacheAwaiterService.awaitCache(adId)).thenReturn(mockFuture);

        // Act
        AdvertisementDTO result = advertisementService.getAdvertisementById(adId);

        // Assert
        assertEquals(adId, result.getId());
        verify(sendCacheService).sendCacheRequest("{\"id\":1,\"status\":false}");
    }

    // Тест для createAdvertisement()
    @Test
    void createAdvertisement_WithValidData_ReturnsDTO() throws IOException {
        // Arrange
        AdvertisementDTO inputDTO = new AdvertisementDTO();
        inputDTO.setSubCategoryId(1L);
        inputDTO.setTitle("Test Title");
        inputDTO.setDescription("Test Description");
        inputDTO.setStatus(true);

        SubCategory subCategory = new SubCategory();
        subCategory.setId(1L);
        when(subCategoryRepository.findById(1L)).thenReturn(Optional.of(subCategory));

        Image mockImage = new Image();
        mockImage.setId(1L);
        when(imageService.createImage(any())).thenReturn(mockImage);

        // Создаем список изображений для сохраненного объявления
        List<Image> mockImages = new ArrayList<>();
        mockImages.add(mockImage);

        // Настраиваем мок для advertisementRepository.save()
        Advertisement savedAdvertisement = new Advertisement();
        savedAdvertisement.setId(1L);
        savedAdvertisement.setTitle(inputDTO.getTitle());
        savedAdvertisement.setDescription(inputDTO.getDescription());
        savedAdvertisement.setStatus(inputDTO.isStatus());
        savedAdvertisement.setSubcategory(subCategory);
        savedAdvertisement.setImageList(mockImages); // Добавляем список изображений

        when(advertisementRepository.save(any(Advertisement.class))).thenReturn(savedAdvertisement);

        // Act
        AdvertisementDTO result = advertisementService.createAdvertisement(
                inputDTO,
                List.of(mock(MultipartFile.class)),
                123L
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.getImagesId());
        assertEquals(1, result.getImagesId().size()); // Проверяем, что список не пуст
        verify(advertisementRepository, times(1)).save(any());
    }


    // Тест для updateAdvertisementById()
    @Test
    void updateAdvertisementById_UpdatesTitle_ReturnsUpdatedDTO() throws Exception {
        // Arrange
        Long adId = 1L;
        Advertisement existingAd = new Advertisement();
        existingAd.setId(adId);
        existingAd.setTitle("Old Title");
        existingAd.setStatus(false); // Установите обязательные поля
        existingAd.setOwnerId(123L); // Пример обязательного поля

        AdvertisementDTO updateDTO = new AdvertisementDTO();
        updateDTO.setTitle("New Title");

        // Настройка моков
        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(existingAd));
        when(advertisementRepository.save(existingAd)).thenReturn(existingAd); // Возвращаем сохраненный объект

        // Act
        AdvertisementDTO result = advertisementService.updateAdvertisementById(adId, updateDTO, null);

        // Assert
        assertEquals("New Title", result.getTitle());
        verify(sendCacheService, times(1)).updateCacheAsync(any());
    }

    // Тест для deleteAdvertisementById()
    @Test
    void deleteAdvertisementById_WhenExists_DeletesEntityAndCache() {
        // Arrange
        Long adId = 1L;
        when(advertisementRepository.existsById(adId)).thenReturn(true);

        // Act
        advertisementService.deleteAdvertisementById(adId);

        // Assert
        verify(advertisementRepository, times(1)).deleteById(adId);
        verify(redisCommands, times(1)).del("advertisement:" + adId);
    }

    // Тест для обработки исключений (например, EntityNotFoundException)
    @Test
    void getAdvertisementById_WhenAdNotFound_ThrowsException() {
        // Arrange
        Long adId = 999L;
        when(advertisementRepository.findById(adId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                advertisementService.getAdvertisementById(adId)
        );
    }
}
