package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubCategoryService {
    private final SubCategoryRepository subCategoryRepository;

    public SubCategoryService(SubCategoryRepository subCategoryRepository) {
        this.subCategoryRepository = subCategoryRepository;
    }

    public List<SubCategory> getAllSubCategories() {
        return subCategoryRepository.findAll();
    }

    public SubCategory getSubCategoryById(Long id) {
        return subCategoryRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Подкатегория не найдена")
        );
    }

    @Transactional
    public SubCategory createSubCategory(SubCategory subCategory) {
        subCategoryRepository.save(subCategory);
        return subCategory;
    }

    @Transactional
    public SubCategory updateSubCategoryById(Long id, SubCategory subCategory) {
        SubCategory updatedSubCategory = getSubCategoryById(id);
        updatedSubCategory.setName(subCategory.getName());
        updatedSubCategory.setParentCategory(subCategory.getParentCategory());
        subCategoryRepository.save(updatedSubCategory);
        return updatedSubCategory;
    }

    @Transactional
    public void deleteSubCategoryById(Long id) {
        subCategoryRepository.deleteById(id);
    }
}
