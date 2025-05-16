package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Shared.Kafka.DTO.Advertisement.ImageDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Repository.ImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private ImageService imageService;

    @Value("${image.base-dir}")
    private String IMAGE_DIR;

    @TempDir
    Path tempDir;

    // Метод для инициализации перед каждым тестом
    @BeforeEach
    void setup() {
        // Указываем имя поля "IMAGE_DIR" как строку
        ReflectionTestUtils.setField(imageService, "IMAGE_DIR", tempDir.toString());
    }

    // Вспомогательный метод для создания тестового изображения
    private Image createTestImage(Long id, String fileName, boolean isPreview) {
        Image image = new Image();
        image.setId(id);
        image.setName(fileName);
        image.setPreviewImage(isPreview);
        return image;
    }

    /*
    0. Метод getImageById
    Что делает: Возвращает изображение (сущность).
    Сценарии для тестирования:
        Возвращает изображение.
        Выбрасывание исключения, если изображение не найдено.
     */

    @Test
    void getImageById_WhenImageExists_ReturnsImage() {
        // Arrange
        Long imageId = 1L;
        Image expectedImage = new Image();
        expectedImage.setId(imageId);
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(expectedImage));

        // Act
        Image result = imageService.getImageById(imageId);

        // Assert
        assertNotNull(result);
        assertEquals(imageId, result.getId());
        verify(imageRepository, times(1)).findById(imageId);
    }

    @Test
    void getImageById_WhenImageNotExists_ThrowsException() {
        // Arrange
        Long imageId = 999L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> imageService.getImageById(imageId));
        verify(imageRepository, times(1)).findById(imageId);
    }
    /* ________________________________________________________________________________________________________________*/

    /*
    1. Метод getImageMetadataByAdvertisementId
    Что делает: Возвращает метаданные изображений для определенного объявления.
    Сценарии для тестирования:
        Успешный возврат списка DTO, если объявление существует.
        Выбрасывание исключения, если объявление не найдено.
        Проверка корректности полей в DTO (например, URL).
     */

    @Test
    void getImageMetadataByAdvertisementId_WhenAdvertisementExists_ReturnsDTOList() {
        // Arrange
        Long adId = 1L;
        Advertisement ad = new Advertisement();
        List<Image> images = List.of(
                createTestImage(1L, "image1.jpg", true),
                createTestImage(2L, "image2.jpg", false)
        );
        ad.setImageList(images);

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));

        // Act
        List<ImageDTO> result = imageService.getImageMetadataByAdvertisementId(adId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("/images/1", result.get(0).getUrl());
        verify(advertisementRepository, times(1)).findById(adId);
    }

    @Test
    void getImageMetadataByAdvertisementId_WhenAdvertisementNotExists_ThrowsException() {
        // Arrange
        Long adId = 999L;
        when(advertisementRepository.findById(adId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                imageService.getImageMetadataByAdvertisementId(adId)
        );
    }
    /* ________________________________________________________________________________________________________________*/

    /*
    2. Метод createImage
    Что делает: Сохраняет изображение на диск и в базу данных.

    Сценарии для тестирования:
        Успешное сохранение изображения.
        Обработка ошибок при записи файла (например, IOException).

     */
    @Test
    void createImage_WhenValidFile_ReturnsSavedImage() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getBytes()).thenReturn(new byte[0]);
        when(file.getContentType()).thenReturn("image/jpeg");

        // Устанавливаем временную директорию для теста
        String testUploadDir = "test-uploads";
        ReflectionTestUtils.setField(imageService, "IMAGE_DIR", testUploadDir);

        // Настраиваем мок репозитория с корректным filePath
        Image savedImage = new Image();
        savedImage.setId(1L);
        savedImage.setName("test.jpg");
        savedImage.setFilePath(testUploadDir + "/123456789_test.jpg"); // Путь должен совпадать с логикой
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

        // Act
        Image result = imageService.createImage(file);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test.jpg", result.getName());
        assertNotNull(result.getFilePath()); // Проверка, что путь не null
        verify(imageRepository, times(1)).save(any(Image.class));

        // Очистка (убедитесь, что файл существует)
        if (result.getFilePath() != null) {
            Files.deleteIfExists(Paths.get(result.getFilePath()));
        }
    }

    @Test
    void createImage_WhenIOException_ThrowsException() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getBytes()).thenThrow(new IOException("File error"));

        // Act & Assert
        assertThrows(IOException.class, () -> imageService.createImage(file));
    }
    /* ________________________________________________________________________________________________________________*/


    /*
    Метод createImages
    Что делает: Сохраняет несколько изображений и возвращает список DTO.
    Сценарии:
        Успешное сохранение нескольких файлов.
        Проверка, что DTO содержат корректные URL.
     */

    @Test
    void createImages_WithValidFiles_ReturnsDTOList() throws IOException {
        // Arrange
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        when(file1.getOriginalFilename()).thenReturn("file1.jpg");
        when(file2.getOriginalFilename()).thenReturn("file2.jpg");

        List<MultipartFile> files = List.of(file1, file2);

        // Настраиваем мок репозитория
        List<Image> mockSavedImages = List.of(
                createTestImage(1L, "file1.jpg", true),
                createTestImage(2L, "file2.jpg", false)
        );
        when(imageRepository.saveAll(anyList())).thenReturn(mockSavedImages);

        // Act
        List<ImageDTO> result = imageService.createImages(files);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).getUrl().startsWith("/images/"));
        verify(imageRepository, times(1)).saveAll(anyList());
    }
    /* ________________________________________________________________________________________________________________*/


    /*
    Метод deleteImageById
    Что делает: Удаляет изображение из БД и файл с диска.
    Сценарии:
        Успешное удаление существующего изображения.
        Игнорирование ошибок при удалении файла (например, файл уже удален).
     */

    @Test
    void deleteImageById_WhenImageExists_DeletesImageAndFile() {
        // Arrange
        Long imageId = 1L;
        Image image = new Image();
        image.setFilePath("test-uploads/1_test.jpg");
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

        // Act
        imageService.deleteImageById(imageId);

        // Assert
        verify(imageRepository, times(1)).delete(image);
        assertFalse(Files.exists(Paths.get(image.getFilePath())));
    }

    @Test
    void deleteImageById_WhenFileMissing_LogsErrorButDeletesEntity() {
        // Arrange
        Long imageId = 1L;
        Image image = new Image();
        image.setFilePath("non-existent-file.jpg");
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

        // Act
        imageService.deleteImageById(imageId);

        // Assert
        verify(imageRepository, times(1)).delete(image);
    }
}
