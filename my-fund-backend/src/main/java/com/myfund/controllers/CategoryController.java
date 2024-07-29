package com.myfund.controllers;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.DTOs.CategoryDTO;
import com.myfund.models.DTOs.CreateCategoryDTO;
import com.myfund.models.User;
import com.myfund.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(categoryService.findAllCategoriesByUser(user), HttpStatus.OK);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable("categoryId") Long categoryId, @AuthenticationPrincipal User user) {
        CategoryDTO categoryDTO = categoryService.findCategoryByIdAndUser(categoryId, user);
        return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CreateCategoryDTO createCategoryDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        CategoryDTO categoryDTO = categoryService.createCategory(createCategoryDTO, user);
        return new ResponseEntity<>(categoryDTO, HttpStatus.CREATED);
    }

    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable("categoryId") Long categoryId, @RequestBody CreateCategoryDTO createCategoryDTO, @AuthenticationPrincipal User user) {
        CategoryDTO categoryDTO = categoryService.updateCategory(categoryId, createCategoryDTO, user);
        return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable("categoryId") Long categoryId, @AuthenticationPrincipal User user) {
        categoryService.deleteCategoryByIdAndUser(categoryId, user);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/categories/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<?> deleteSubcategory(@PathVariable("categoryId") Long categoryId, @PathVariable("subcategoryId") Long subcategoryId, @AuthenticationPrincipal User user) {
        categoryService.deleteSubcategoryByIdsAndUser(categoryId, subcategoryId, user);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
