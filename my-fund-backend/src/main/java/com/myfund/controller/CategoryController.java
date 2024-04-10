package com.myfund.controller;

import com.myfund.model.User;
import com.myfund.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories/")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/add-categories-with-subcategory")
    public ResponseEntity<?> createCategoryWithSubCategory(@RequestParam(value = "category", required = false) String category, @RequestParam(value = "subcategory", required = false) String subcategory, @AuthenticationPrincipal User user) {
        try {
            categoryService.saveCategoryWithSubCategory(category, subcategory, user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create category with subcategory: " + e.getMessage());
        }
    }

}
