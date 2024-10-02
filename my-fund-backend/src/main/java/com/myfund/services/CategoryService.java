package com.myfund.services;

import com.myfund.exceptions.CategoryNotFoundException;
import com.myfund.exceptions.CategoryNotUniqueException;
import com.myfund.exceptions.InvalidInputException;
import com.myfund.exceptions.SubcategoryNotFoundException;
import com.myfund.models.Category;
import com.myfund.models.DTOs.CategoryDTO;
import com.myfund.models.DTOs.CreateCategoryDTO;
import com.myfund.models.DTOs.mappers.CategoryMapper;
import com.myfund.models.DTOs.mappers.SubCategoryMapper;
import com.myfund.models.SubCategory;
import com.myfund.models.User;
import com.myfund.repositories.CategoryRepository;
import com.myfund.repositories.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final SubCategoryRepository subCategoryRepository;
    @Lazy
    private final BudgetService budgetService;

    public List<Category> findAllCategoriesByUser(User user) {
        List<Category> category = categoryRepository.findAllCategoriesByUser(user);
        log.info("Retrieved {} categories for user with ID: {}", category.size(), user.getId());
        return category;
    }

    public Category findCategoryByIdAndUser(Long categoryId, User user) {
        log.debug("Starting to find category by ID: {} for user ID: {}", categoryId, user.getId());

        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> {
                    log.warn("Category not found for user with ID: {} and category ID: {}", user.getId(), categoryId);
                    return new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
                });

        log.info("Category found for user with ID: {} and category ID: {}", user.getId(), categoryId);
        return category;
    }

    public Category createCategory(Category category, User user) throws InvalidInputException {
        log.debug("Starting to create a new category with name: {} for user ID: {}", category.getName(), user.getId());
        if (category.getName() == null || category.getName().isEmpty()) {
            log.warn("Category name is required.");
            throw new InvalidInputException("Category name is required");
        }
        categoryRepository.findByNameAndUser(category.getName(), user)
                .ifPresent(existingCategory -> {
                    log.warn("Category creation attempt failed. Category with name: {} already exists for user ID: {}", category.getName(), user.getId());
                    throw new CategoryNotUniqueException("Category with name: " + existingCategory.getName() + " is not unique");
                });
        Category inicializedCategory = Category.create(category, user);
        Category savedCatgory=categoryRepository.save(inicializedCategory);
        log.info("New category created with name: {} for user ID: {}", category.getName(), user.getId());
        return savedCatgory;
    }

    public Category updateCategory(Long categoryId, Category category, User user) {
        log.debug("Starting to update category with ID: {} for user ID: {}", categoryId, user.getId());
        Category existingCategory = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> {
                    log.warn("Category not found for user ID: {} and category ID: {}", user.getId(), categoryId);
                    return new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
                });
        Category updatedCategory = Category.update(existingCategory, category);
        Category savedCategory = categoryRepository.save(updatedCategory);
        log.info("Category with ID: {} updated for user ID: {}", categoryId, user.getId());
        return savedCategory;
    }


    @Transactional
    public void deleteCategoryByIdAndUser(Long categoryId, User user) {
        log.debug("Starting to delete category with ID: {} for user ID: {}", categoryId, user.getId());

        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> {
                    log.warn("Failed to delete category. Category not found for user ID: {} and category ID: {}", user.getId(), categoryId);
                    return new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
                });

        budgetService.updateExpensesCategoryIdToNull(category.getId());
        budgetService.updateIncomesCategoryIdToNull(category.getId());
        categoryRepository.delete(category);

        log.info("Category with ID: {} deleted for user ID: {}", categoryId, user.getId());
    }

    private Category getCategoryByIdAndUser(Long categoryId, User user) {
        log.debug("Starting to find category with ID: {} for user ID: {}", categoryId, user.getId());
        return categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> {
                    log.info("Category with ID: {} not found for user ID: {}", categoryId, user.getId());
                    return new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
                });
    }

    public boolean isSubcategoryRelatedToCategory(Long subcategoryId, Long categoryId, User user) {

        log.debug("Checking if subcategory with ID: {} is related to category ID: {} for user ID: {}", subcategoryId, categoryId, user.getId());
        Category category  = getCategoryByIdAndUser(categoryId, user);

        boolean isRelated = category.getSubCategories().stream().anyMatch(subCategory -> subCategory.getId().equals(subcategoryId));
        log.info("Subcategory with ID: {} is {}related to category ID: {} for user ID: {}", subcategoryId, isRelated ? "" : "not ", categoryId, user.getId());
        return isRelated;
    }

    public void deleteSubcategoryByIdsAndUser(Long categoryId, Long subcategoryId, User user) {
        log.debug("Starting to delete subcategory with ID: {} from category ID: {} for user ID: {}", subcategoryId, categoryId, user.getId());

        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> {
                    log.warn("Category not found for user with ID: {} and category ID: {}. Unable to delete subcategory with ID: {}", user.getId(), categoryId, subcategoryId);
                    return new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
                });

        log.debug("Category found. Proceeding with subcategory removal.");

        SubCategory subCategoryToRemove = category.getSubCategories().stream()
                .filter(subCategory -> subCategory.getId().equals(subcategoryId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Subcategory not found for user with ID: {}, category ID: {}, and subcategory ID: {}", user.getId(), categoryId, subcategoryId);
                    return new SubcategoryNotFoundException("Subcategory not found for user with ID: " + user.getId() + ", category ID: " + categoryId + " and subcategory ID: " + subcategoryId);
                });

        budgetService.updateExpensesSubcategoryIdToNull(subcategoryId);
        budgetService.updateIncomesSubcategoryIdToNull(subcategoryId);

        category.getSubCategories().remove(subCategoryToRemove);
        subCategoryRepository.delete(subCategoryToRemove);

        log.info("Subcategory with ID: {} removed from category ID: {} for user ID: {}", subcategoryId, categoryId, user.getId());
    }
}