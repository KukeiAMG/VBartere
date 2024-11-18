package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.DTO.AdvertisementDTO;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ImageService imageService;

    public AdvertisementService(AdvertisementRepository advertisementRepository, SubCategoryRepository subCategoryRepository, ImageService imageService) {
        this.advertisementRepository = advertisementRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.imageService = imageService;
    }

    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    public Advertisement findById(Long id) {
        return advertisementRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public AdvertisementDTO getAdvertisementById(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Advertisement not found"));

        // Преобразуем сущность в DTO с использованием идентификаторов изображений
        List<Long> imageIds = advertisement.getImageList().stream()
                .map(Image::getId)
                .collect(Collectors.toList());

        return new AdvertisementDTO(advertisement.getTitle(), advertisement.getDescription(), advertisement.getSubcategory().getId(), advertisement.getOwnerId(),
                advertisement.getBuyersId(), imageIds, advertisement.getStatus());
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
    public Advertisement updateAdvertisementById(Long adId, AdvertisementDTO advertisementDTO, List<MultipartFile> files) throws IOException {
        Advertisement advertisement = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        SubCategory subCategory = subCategoryRepository.findById(advertisementDTO.getSubCategoryId())
                .orElseThrow(() -> new RuntimeException("Подкатегория не найдена"));

        advertisement.setTitle(advertisementDTO.getTitle());
        advertisement.setDescription(advertisementDTO.getDescription());
        advertisement.setSubcategory(subCategory);
        advertisement.setOwnerId(advertisementDTO.getOwnerId());
        advertisement.setStatus(advertisementDTO.isStatus());

        List<Image> images = new ArrayList<>();
        for (MultipartFile file : files) {
            Image image = imageService.createImage(file);
            image.setAdvertisement(advertisement);
            images.add(image);
        }

        if (!images.isEmpty()) {
            images.getFirst().setPreviewImage(true);
            advertisement.setImageList(images);
        }

        return advertisementRepository.save(advertisement);
    }

    @Transactional
    public void deleteAdvertisementById(Long id) {
        advertisementRepository.deleteById(id);
    }
}
