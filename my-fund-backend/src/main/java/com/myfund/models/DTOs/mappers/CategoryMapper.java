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
    public static Category toModel(CreateCategoryDTO createCategoryDTO) {
        List<SubCategory> subCategoryList = Optional.ofNullable(createCategoryDTO.getSubCategories())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(SubCategoryMapper::toModel)
                .collect(Collectors.toList());
        Category category = Category.builder().name(createCategoryDTO.getName()).subCategories(subCategoryList).build();
        return category;
    }

    public static CategoryDTO toDTO(Category category) {
        List<SubCategoryDTO> subCategoryDTOList = category.getSubCategories().stream()
                .map(subCategory -> SubCategoryDTO.builder()
                        .id(subCategory.getId())
                        .name(subCategory.getName())
                        .build())
                .collect(Collectors.toList());

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .subCategories(subCategoryDTOList)
                .build();
    }

    public static List<CategoryDTO> toListDTO(List<Category> categoryList) {
        return categoryList.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .subCategories(SubCategoryMapper.toListDTO(category.getSubCategories()))
                        .build())
                .collect(Collectors.toList());
    }
}
