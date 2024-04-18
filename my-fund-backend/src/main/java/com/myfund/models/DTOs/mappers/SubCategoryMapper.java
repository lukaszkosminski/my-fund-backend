package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateSubCategoryDTO;
import com.myfund.models.SubCategory;

public class SubCategoryMapper {

    public static SubCategory createSubCategoryMapToSubcategory(CreateSubCategoryDTO createSubCategoryDTO) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(createSubCategoryDTO.getName());
        return subCategory;
    }


}
