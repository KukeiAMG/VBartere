package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Repository.ImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final AdvertisementRepository advertisementRepository;

    public ImageService(ImageRepository imageRepository, AdvertisementRepository advertisementRepository) {
        this.imageRepository = imageRepository;
        this.advertisementRepository = advertisementRepository;
    }

    @Transactional
    public Image createImage(String url, Long advertisementId) {
        // Проверка существования объявления
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        // Создание нового объекта Image
        Image image = new Image();
        image.setUrl(url);
        image.setAdvertisement(advertisement);

        // Сохранение изображения в базе данных
        return imageRepository.save(image);
    }
}
