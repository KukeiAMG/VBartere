package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.Category;
import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CategorySubCategoryService {
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final SubCategoryRepository subCategoryRepository;

    public CategorySubCategoryService(CategoryService categoryService, SubCategoryService subCategoryService, SubCategoryRepository subCategoryRepository) {
        this.categoryService = categoryService;
        this.subCategoryService = subCategoryService;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Transactional
    public void addSubCategoryToCategory(Long categoryId, Long subCategoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        SubCategory subCategory = subCategoryService.getSubCategoryById(subCategoryId);
        subCategory.setParentCategory(category);
        subCategoryRepository.save(subCategory);
    }
}
