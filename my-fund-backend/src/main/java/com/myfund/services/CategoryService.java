package com.myfund.services;

import com.myfund.exceptions.CategoryNotUniqueException;
import com.myfund.models.Category;
import com.myfund.models.DTOs.CategoryDTO;
import com.myfund.models.DTOs.CreateCategoryDTO;
import com.myfund.models.DTOs.mappers.CategoryMapper;
import com.myfund.models.DTOs.mappers.SubCategoryMapper;
import com.myfund.models.SubCategory;
import com.myfund.models.User;
import com.myfund.repositories.CategoryRepository;
import com.myfund.repositories.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    public List<CategoryDTO> findAllCategoriesByUser(User user) {
        return CategoryMapper.categoryListMapToCategoryListDTO(categoryRepository.findAllCategoriesByUser(user));

    }

    public Optional<CategoryDTO> findCategoryByIdAndUser(Long categoryId, User user) {
        Optional<Category> existingCategoryOpt = categoryRepository.findByIdAndUser(categoryId, user);
        if (existingCategoryOpt.isPresent()) {
            return Optional.of(CategoryMapper.categoryMapToCategoryDTO(existingCategoryOpt.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<CategoryDTO> createCategory(CreateCategoryDTO createCategoryDTO, User user) {
        Optional<Category> existingCategory = categoryRepository.findByNameAndUser(createCategoryDTO.getName(), user);
        Category category;
        if (existingCategory.isPresent()) {
            throw new CategoryNotUniqueException("category is not unique");
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
            return Optional.of(CategoryMapper.categoryMapToCategoryDTO(category));
        }
    }

    public Optional<CategoryDTO> updateCategory(Long categoryId, CreateCategoryDTO createCategoryDTO, User user) {
        Optional<Category> existingCategoryOpt = categoryRepository.findByIdAndUser(categoryId, user);
        if (existingCategoryOpt.isPresent()) {
            Category category = existingCategoryOpt.get();
            category.setName(createCategoryDTO.getName());

            List<SubCategory> updatedSubCategories = new ArrayList<>();
            createCategoryDTO.getSubCategories().forEach(createSubCategoryDTO -> {
                Optional<SubCategory> existingSubCategory = category.getSubCategories().stream()
                        .filter(subCategory -> subCategory.getName().equals(createSubCategoryDTO.getName()))
                        .findFirst();
                if (existingSubCategory.isPresent()) {
                    updatedSubCategories.add(existingSubCategory.get());
                } else {
                    SubCategory newSubCategory = SubCategoryMapper.createSubCategoryMapToSubcategory(createSubCategoryDTO);
                    newSubCategory.setCategory(category);
                    updatedSubCategories.add(newSubCategory);
                }
            });

            category.getSubCategories().removeIf(subCategory ->
                    updatedSubCategories.stream().noneMatch(updatedSubCategory ->
                            updatedSubCategory.getName().equals(subCategory.getName())));

            category.getSubCategories().clear();
            category.getSubCategories().addAll(updatedSubCategories);

            categoryRepository.save(category);
            CategoryDTO categoryDTO = CategoryMapper.categoryMapToCategoryDTO(category);
            return Optional.of(categoryDTO);
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteCategoryByIdAndUser(Long categoryId, User user) {
        Optional<Category> existingCategoryOpt = categoryRepository.findByIdAndUser(categoryId, user);
        if (existingCategoryOpt.isPresent()) {
            Category category = existingCategoryOpt.get();
            categoryRepository.delete(category);
        } else {
            throw new RuntimeException("Category not found");
        }
    }
    public Optional<Category> getCategoryByIdAndUser(Long categoryId, User user) {
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
}
