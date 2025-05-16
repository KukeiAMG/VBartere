package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.Category;
import com.vbartere.Advertisement.Repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Категория не найдена"));
    }

    @Transactional
    public Category createCategory(Category category) {
        categoryRepository.save(category);
        return category;
    }

    @Transactional
    public Category updateCategoryById(Long id, Category category) {
        Category updatedCategory = getCategoryById(id);
        updatedCategory.setName(category.getName());
        categoryRepository.save(updatedCategory);
        return updatedCategory;
    }

    @Transactional
    public void deleteCategoryById(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Категория не найдена");
        }
    }
}
