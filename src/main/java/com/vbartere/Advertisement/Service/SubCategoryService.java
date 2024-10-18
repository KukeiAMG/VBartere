package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class SubCategoryService {
    private final SubCategoryRepository subCategoryRepository;

    public SubCategoryService(SubCategoryRepository subCategoryRepository) {
        this.subCategoryRepository = subCategoryRepository;
    }


}
