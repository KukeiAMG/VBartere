package com.vbartere.Advertisement.Controller;


import com.vbartere.Advertisement.Model.SubCategory;
import com.vbartere.Advertisement.Service.SubCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategory")
public class SubCategoryController {
    private final SubCategoryService subCategoryService;

    public SubCategoryController(SubCategoryService subCategoryService) {
        this.subCategoryService = subCategoryService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<SubCategory>> getAllSubCategories() {
        List<SubCategory> subCategory = subCategoryService.getAllSubCategories();
        return ResponseEntity.ok(subCategory);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubCategory> getSubCategoryById(@PathVariable("id") Long id) {
        SubCategory subCategory = subCategoryService.getSubCategoryById(id);
        return ResponseEntity.ok(subCategory);
    }

    @PostMapping("/create")
    public ResponseEntity<SubCategory> createSubCategory(@RequestBody SubCategory subCategory) {
        SubCategory newSubCategory = subCategoryService.createSubCategory(subCategory);
        return ResponseEntity.ok(newSubCategory);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<SubCategory> updateSubCategoryById(@PathVariable("id") Long id,
                                                             @RequestBody SubCategory subCategory) {
        SubCategory updatedSubCategory = subCategoryService.updateSubCategoryById(id, subCategory);
        return ResponseEntity.ok(updatedSubCategory);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<SubCategory> deleteCategory(@PathVariable("id") Long id) {
        subCategoryService.deleteSubCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}
