package com.myfund.service;

import com.myfund.model.Category;
import com.myfund.model.SubCategory;
import com.myfund.model.User;
import com.myfund.repository.CategoryRepository;
import com.myfund.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public Category saveCategoryWithSubCategory(String categoryName, String subCategoryName, User user) {
        Optional<Category> existingCategoryOpt = categoryRepository.findByNameAndUser(categoryName, user);
        Category category;

        if (existingCategoryOpt.isPresent()) {
            category = existingCategoryOpt.get();
        } else {
            category = new Category();
            category.setName(categoryName);
            category.setUser(user);
            category.setSubCategoryList(new ArrayList<>());
            category = categoryRepository.save(category);
        }

        boolean subCategoryExists = category.getSubCategoryList().stream()
                .anyMatch(subCategory -> subCategory.getName().equals(subCategoryName));

        if (!subCategoryExists) {
            SubCategory subCategory = new SubCategory();
            subCategory.setName(subCategoryName);
            subCategory.setCategory(category);
            subCategory = subCategoryRepository.save(subCategory);
            category.getSubCategoryList().add(subCategory);
            category = categoryRepository.save(category);
        }

        return category;
    }
}
