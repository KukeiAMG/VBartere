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
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ImageRepository imageRepository;
    // RestTemplate для вызова внешнего микросервиса UserService
    private final RestTemplate restTemplate;
    private final String USER_URL = "http://localhost:8080/user_service/api/";

    private Image toImageEntity(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());
        return image;
    }

    public AdvertisementService(AdvertisementRepository advertisementRepository, CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository, ImageRepository imageRepository, RestTemplate restTemplate) {
        this.advertisementRepository = advertisementRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.imageRepository = imageRepository;
        this.restTemplate = restTemplate;
    }

    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    public Advertisement getAdvertisementById(Long id) {
        return advertisementRepository.findById(id).orElse(null);
    }

    @Transactional
    public Advertisement createAdvertisement(AdvertisementDTO advertisementDTO) {

        SubCategory subCategory = subCategoryRepository.findById(advertisementDTO.getSubCategoryId())
                .orElseThrow(() -> new RuntimeException("Подкатегория не найдена"));
        System.out.println(subCategory);

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(advertisementDTO.getTitle());
        advertisement.setDescription(advertisementDTO.getDescription());
        advertisement.setSubcategory(subCategory);
        advertisement.setUserId(advertisementDTO.getUserId());
        advertisement.setStatus(advertisementDTO.isStatus());

        for (Long imageId : advertisementDTO.getImageIds()) {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Изображение не найдено"));
            List<Image> imageList = new ArrayList<>();
            imageList.add(image);
            advertisement.setImageList(imageList);
        }
        return advertisementRepository.save(advertisement);
    }

    @Transactional
    public void updateAdvertisementById(Long id, Advertisement advertisement) {
        Advertisement updatedAdvertisement = getAdvertisementById(id);
        if (updatedAdvertisement != null) {
            updatedAdvertisement.setTitle(advertisement.getTitle());
            updatedAdvertisement.setDescription(advertisement.getDescription());
            updatedAdvertisement.setSubcategory(advertisement.getSubcategory());
            updatedAdvertisement.setImageList(advertisement.getImageList());
        }
    }
}
