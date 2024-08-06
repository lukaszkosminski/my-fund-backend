package com.myfund.controllers;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.Category;
import com.myfund.models.DTOs.CategoryDTO;
import com.myfund.models.DTOs.CreateCategoryDTO;
import com.myfund.models.DTOs.mappers.CategoryMapper;
import com.myfund.models.User;
import com.myfund.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
        List<Category> allCategoriesByUser = categoryService.findAllCategoriesByUser(user);
        return new ResponseEntity<>(CategoryMapper.toListDTO(allCategoriesByUser), HttpStatus.OK);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable("categoryId") Long categoryId, @AuthenticationPrincipal User user) {
        Category category = categoryService.findCategoryByIdAndUser(categoryId, user);
        return new ResponseEntity<>(CategoryMapper.toDTO(category), HttpStatus.OK);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody @Valid CreateCategoryDTO createCategoryDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        Category category = categoryService.createCategory(CategoryMapper.toModel(createCategoryDTO), user);
        return new ResponseEntity<>(CategoryMapper.toDTO(category), HttpStatus.CREATED);
    }

    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable("categoryId") Long categoryId, @RequestBody @Valid CreateCategoryDTO createCategoryDTO, @AuthenticationPrincipal User user) {
        Category category = categoryService.updateCategory(categoryId, CategoryMapper.toModel(createCategoryDTO), user);
        return new ResponseEntity<>(CategoryMapper.toDTO(category), HttpStatus.OK);
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
