package com.myfund.models.DTOs.mappers;

import com.myfund.models.Category;
import com.myfund.models.DTOs.CreateSubCategoryDTO;
import com.myfund.models.DTOs.CategoryDTO;
import com.myfund.models.DTOs.CreateCategoryDTO;
import com.myfund.models.DTOs.SubCategoryDTO;
import com.myfund.models.SubCategory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CategoryMapper {
    public static Category createCategoryDTOMapToCategory(CreateCategoryDTO createCategoryDTO) {
        Category category = new Category();
        List<SubCategory> subCategoryList = Optional.ofNullable(createCategoryDTO.getSubCategories())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(CategoryMapper::createSubCategoryDTOToSubCategory)
                .collect(Collectors.toList());
        category.setName(createCategoryDTO.getName());
        category.setSubCategories(subCategoryList);
        return category;
    }

    public static CategoryDTO categoryMapToCategoryDTO(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());

        List<SubCategoryDTO> subCategoryDTOList = category.getSubCategories().stream()
                .map(subCategory -> {
                    SubCategoryDTO subCategoryDTO = new SubCategoryDTO();
                    subCategoryDTO.setId(subCategory.getId());
                    subCategoryDTO.setName(subCategory.getName());
                    return subCategoryDTO;
                })
                .collect(Collectors.toList());

        categoryDTO.setSubCategories(subCategoryDTOList);
        return categoryDTO;
    }

    public static List<CategoryDTO> categoryListMapToCategoryListDTO(List<Category> categoryList) {
        return categoryList.stream()
                .map(category -> {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.setName(category.getName());
                    categoryDTO.setId(category.getId());
                    categoryDTO.setSubCategories(CategoryMapper.subCategoryListMapToSubCategoryListDTO(category.getSubCategories()));
                    return categoryDTO;
                })
                .collect(Collectors.toList());
    }

    public static List<SubCategoryDTO> subCategoryListMapToSubCategoryListDTO(List<SubCategory> subCategoryList) {
        return subCategoryList.stream()
                .map(subCategory -> {
                    SubCategoryDTO subCategoryDTO = new SubCategoryDTO();
                    subCategoryDTO.setId(subCategory.getId());
                    subCategoryDTO.setName(subCategory.getName());
                    return subCategoryDTO;
                })
                .collect(Collectors.toList());
    }

    private static SubCategory createSubCategoryDTOToSubCategory(CreateSubCategoryDTO createSubCategoryDTO) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(createSubCategoryDTO.getName());
        return subCategory;
    }

}
