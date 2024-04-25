package com.myfund.services;

import com.myfund.exceptions.CategoryNotFoundException;
import com.myfund.exceptions.CategoryNotUniqueException;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    private final BudgetService budgetService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository, @Lazy BudgetService budgetService) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.budgetService = budgetService;
    }

    public List<CategoryDTO> findAllCategoriesByUser(User user) {
        return CategoryMapper.categoryListMapToCategoryListDTO(categoryRepository.findAllCategoriesByUser(user));

    }

    public CategoryDTO findCategoryByIdAndUser(Long categoryId, User user) {
        Optional<Category> existingCategoryOpt = categoryRepository.findByIdAndUser(categoryId, user);
        if (existingCategoryOpt.isPresent()) {
            return CategoryMapper.categoryMapToCategoryDTO(existingCategoryOpt.get());
        } else {
            throw new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
        }
    }

    public CategoryDTO createCategory(CreateCategoryDTO createCategoryDTO, User user) {
        Optional<Category> existingCategory = categoryRepository.findByNameAndUser(createCategoryDTO.getName(), user);
        Category category;
        if (existingCategory.isPresent()) {
            throw new CategoryNotUniqueException("Category with name: " + existingCategory.get().getName() + " is not unique");
        } else {
            category = CategoryMapper.createCategoryDTOMapToCategory(createCategoryDTO);
            category.setUser(user);
            Category savedCategory = categoryRepository.save(category);
            ArrayList<SubCategory> subCategoryList = new ArrayList<>();
            for (SubCategory subCategory : category.getSubCategories()) {

                subCategory.setCategory(savedCategory);
                subCategoryList.add(subCategory);
            }
            category.getSubCategories().clear();
            category.getSubCategories().addAll(subCategoryList);
            categoryRepository.save(category);
            return CategoryMapper.categoryMapToCategoryDTO(category);
        }
    }

    public CategoryDTO updateCategory(Long categoryId, CreateCategoryDTO createCategoryDTO, User user) {
        Optional<Category> existingCategoryOpt = categoryRepository.findByIdAndUser(categoryId, user);
        if (existingCategoryOpt.isPresent()) {
            Category category = existingCategoryOpt.get();
            category.setName(createCategoryDTO.getName());

            List<SubCategory> existingSubCategories = category.getSubCategories();
            createCategoryDTO.getSubCategories().forEach(createSubCategoryDTO -> {
                Optional<SubCategory> existingSubCategory = existingSubCategories.stream()
                        .filter(subCategory -> subCategory.getName().equals(createSubCategoryDTO.getName()))
                        .findFirst();
                if (!existingSubCategory.isPresent()) {
                    SubCategory newSubCategory = SubCategoryMapper.createSubCategoryMapToSubcategory(createSubCategoryDTO);
                    newSubCategory.setCategory(category);
                    existingSubCategories.add(newSubCategory);
                }
            });

            categoryRepository.save(category);
            CategoryDTO categoryDTO = CategoryMapper.categoryMapToCategoryDTO(category);
            return categoryDTO;
        } else {
            throw new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
        }
    }

    @Transactional
    public void deleteCategoryByIdAndUser(Long categoryId, User user) {
        Optional<Category> existingCategoryOpt = categoryRepository.findByIdAndUser(categoryId, user);
        if (existingCategoryOpt.isPresent()) {
            Category category = existingCategoryOpt.get();
            budgetService.updateExpensesCategoryIdToNull(category.getId());
            budgetService.updateIncomesCategoryIdToNull(category.getId());
            categoryRepository.delete(category);
        } else {
            throw new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
        }
    }

    private Optional<Category> getCategoryByIdAndUser(Long categoryId, User user) {
        Optional<Category> categoryOpt = categoryRepository.findByIdAndUser(categoryId, user);
        if (categoryOpt.isPresent()) {
            return categoryOpt;
        } else {
            return Optional.empty();
        }
    }

    public boolean isSubcategoryRelatedToCategory(Long subcategoryId, Long categoryId, User user) {
        Optional<Category> categoryOpt = getCategoryByIdAndUser(categoryId, user);
        if (categoryOpt.isEmpty()) {
            return false;
        }
        Category category = categoryOpt.get();
        return category.getSubCategories().stream()
                .anyMatch(subCategory -> subCategory.getId().equals(subcategoryId));
    }

    public void deleteSubcategoryByIdsAndUser(Long categoryId, Long subcategoryId, User user) {
        log.debug("Starting to delete subcategory with ID: {} from category ID: {} for user ID: {}", subcategoryId, categoryId, user.getId());
        Optional<Category> categoryOpt = categoryRepository.findByIdAndUser(categoryId, user);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
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
        } else {
            log.warn("Category not found for user with ID: {} and category ID: {}. Unable to delete subcategory with ID: {}", user.getId(), categoryId, subcategoryId);
            throw new CategoryNotFoundException("Category not found for user with ID: " + user.getId() + " and category ID: " + categoryId);
        }
    }
}
