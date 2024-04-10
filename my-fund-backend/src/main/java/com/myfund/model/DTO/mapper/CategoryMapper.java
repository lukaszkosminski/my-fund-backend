package com.myfund.model.DTO.mapper;

import com.myfund.model.Category;
import com.myfund.model.DTO.CreateCategoryDTO;

public class CategoryMapper {
    public static Category createCategoryDTOMapToCategory(CreateCategoryDTO createCategoryDTO) {
        Category category = new Category();
        category.setName(createCategoryDTO.getName());
        return category;
    }
}
