package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateSubCategoryDTO;
import com.myfund.models.DTOs.SubCategoryDTO;
import com.myfund.models.SubCategory;

import java.util.List;
import java.util.stream.Collectors;

public class SubCategoryMapper {

    public static SubCategory toModel(CreateSubCategoryDTO createSubCategoryDTO) {
        return SubCategory.builder()
                .name(createSubCategoryDTO.getName())
                .build();
    }

    public static List<SubCategoryDTO> toListDTO(List<SubCategory> subCategoryList) {
        return subCategoryList.stream()
                .map(subCategory -> SubCategoryDTO.builder()
                        .id(subCategory.getId())
                        .name(subCategory.getName())
                        .build())
                .collect(Collectors.toList());
    }

}
