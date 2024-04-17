package com.myfund.models.DTO.mappers;

import com.myfund.models.DTO.CreateSubCategoryDTO;
import com.myfund.models.SubCategory;

public class SubCategoryMapper {

    public static SubCategory createSubCategoryMapToSubcategory(CreateSubCategoryDTO createSubCategoryDTO) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(createSubCategoryDTO.getName());
        return subCategory;
    }


}
