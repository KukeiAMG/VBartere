package com.vbartere.Advertisement.Service;

import com.vbartere.Advertisement.Model.Category;
import com.vbartere.Advertisement.Repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Категория не найдена"));
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
        categoryRepository.deleteById(id);
    }
}
