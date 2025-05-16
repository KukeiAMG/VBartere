package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Repository.SubCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubCategoryService {
    private final SubCategoryRepository subCategoryRepository;

    public SubCategoryService(SubCategoryRepository subCategoryRepository) {
        this.subCategoryRepository = subCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<SubCategory> getAllSubCategories() {
        return subCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SubCategory getSubCategoryById(Long id) {
        return subCategoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Подкатегория не найдена")
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
        if (subCategoryRepository.existsById(id)) {
            subCategoryRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Подкатегория не найдена");
        }
    }
}
