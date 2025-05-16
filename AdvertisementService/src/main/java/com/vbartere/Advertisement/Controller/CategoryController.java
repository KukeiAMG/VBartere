package com.vbartere.Advertisement.Controller;

import com.vbartere.Advertisement.Model.Category;
import com.vbartere.Advertisement.Service.CategoryService;
import com.vbartere.Advertisement.Service.CategorySubCategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


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
    public ResponseEntity<?> getSubCategoryById(@PathVariable("id") Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
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
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        try {
            categoryService.deleteCategoryById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{categoryId}/subcategories/{subCategoryId}")
    public ResponseEntity<?> addSubCategoryToCategory(@PathVariable("categoryId") Long categoryId,
                                                      @PathVariable("subCategoryId") Long subCategoryId) {
        try {
            categorySubCategoryService.addSubCategoryToCategory(categoryId, subCategoryId);
            return ResponseEntity.ok("Подкатегория успешно добавлена к категории");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
