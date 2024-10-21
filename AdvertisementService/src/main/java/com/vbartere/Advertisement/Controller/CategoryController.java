package com.vbartere.Advertisement.Controller;

import com.vbartere.Advertisement.Model.Category;
import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Service.CategoryService;
import com.vbartere.Advertisement.Service.CategorySubCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategorySubCategoryService categorySubCategoryService;

    public CategoryController(CategoryService categoryService, CategorySubCategoryService categorySubCategoryService) {
        this.categoryService = categoryService;
        this.categorySubCategoryService = categorySubCategoryService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAllSubCategories() {
        List<Category> subCategory = categoryService.getAllCategories();
        return ResponseEntity.ok(subCategory);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getSubCategoryById(@PathVariable("id") Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping("/create")
    public ResponseEntity<Category> createSubCategory(@RequestBody Category category) {
        Category newCategory = categoryService.createCategory(category);
        return ResponseEntity.ok(newCategory);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Category> updateSubCategoryById(@PathVariable("id") Long id,
                                                             @RequestBody Category category) {
        Category updatedSubCategory = categoryService.updateCategoryById(id, category);
        return ResponseEntity.ok(updatedSubCategory);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<SubCategory> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{categoryId}/subcategories/{subCategoryId}")
    public ResponseEntity<String> addSubCategoryToCategory(@PathVariable("categoryId") Long categoryId,
                                                           @PathVariable("subCategoryId") Long subCategoryId) {
        try {
            categorySubCategoryService.addSubCategoryToCategory(categoryId, subCategoryId);
            return ResponseEntity.ok("Подкатегория успешно добавлена к категории");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
