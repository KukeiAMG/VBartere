package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.DTO.AdvertisementDTO;
import com.vbartere.Advertisement.Model.Advertisement;
import com.vbartere.Advertisement.Model.Image;
import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import com.vbartere.Advertisement.Repository.CategoryRepository;
import com.vbartere.Advertisement.Repository.ImageRepository;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final SubCategoryRepository subCategoryRepository;
    // RestTemplate для вызова внешнего микросервиса UserService
    private final RestTemplate restTemplate;
    private final String USER_URL = "http://localhost:8080/user_service/api/";
    private final ImageService imageService;

    public AdvertisementService(AdvertisementRepository advertisementRepository, SubCategoryRepository subCategoryRepository, RestTemplate restTemplate, ImageService imageService) {
        this.advertisementRepository = advertisementRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.restTemplate = restTemplate;
        this.imageService = imageService;
    }

    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    public Advertisement getAdvertisementById(Long id) {
        return advertisementRepository.findById(id).orElse(null);
    }

    @Transactional
    public Advertisement createAdvertisement(AdvertisementDTO advertisementDTO, List<MultipartFile> files) throws IOException {
        SubCategory subCategory = subCategoryRepository.findById(advertisementDTO.getSubCategoryId())
                .orElseThrow(() -> new RuntimeException("Подкатегория не найдена"));

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(advertisementDTO.getTitle());
        advertisement.setDescription(advertisementDTO.getDescription());
        advertisement.setSubcategory(subCategory);
        advertisement.setOwnerId(advertisementDTO.getOwnerId());
        advertisement.setStatus(advertisementDTO.isStatus());

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
