package com.myfund.model.DTO.mapper;

import com.myfund.model.DTO.CreateSubCategoryDTO;
import com.myfund.model.SubCategory;

public class SubCategoryMapper {

    public static SubCategory createSubCategoryMapToSubcategory(CreateSubCategoryDTO createSubCategoryDTO) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(createSubCategoryDTO.getName());
        return subCategory;
    }
}
