package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Repository.AdvertisementRepository;
import org.springframework.stereotype.Service;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;

    public AdvertisementService(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    
}
