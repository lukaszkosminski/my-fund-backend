package com.myfund.model.DTO.mapper;

import com.myfund.model.DTO.CreateCategoryDTO;
import com.myfund.model.SubCategory;

public class SubCategoryMapper {

    public static SubCategory createSubCategoryMapToSubCategoryDTO(CreateCategoryDTO createCategoryDTO) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(subCategory.getName());

        return subCategory;
    }
}

