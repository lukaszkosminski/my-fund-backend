package com.myfund.model.DTO.mapper;

import com.myfund.model.Category;
import com.myfund.model.DTO.CreateSubCategoryDTO;
import com.myfund.model.DTO.CategoryDTO;
import com.myfund.model.DTO.CreateCategoryDTO;
import com.myfund.model.DTO.SubCategoryDTO;
import com.myfund.model.SubCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {
    public static Category createCategoryDTOMapToCategory(CreateCategoryDTO createCategoryDTO) {
        Category category = new Category();
        ArrayList<SubCategory> subCategoryList = new ArrayList<>();
        if (createCategoryDTO.getSubCategories() != null) {
            for (CreateSubCategoryDTO createSubCategoryDTO : createCategoryDTO.getSubCategories()) {
                SubCategory subCategory = createSubCategoryDTOToSubCategory(createSubCategoryDTO);
                subCategoryList.add(subCategory);
            }
        }
        category.setName(createCategoryDTO.getName());
        category.setSubCategories(subCategoryList);
        return category;
    }

    private static SubCategory createSubCategoryDTOToSubCategory(CreateSubCategoryDTO createSubCategoryDTO) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(createSubCategoryDTO.getName());
        return subCategory;
    }

    public static CategoryDTO categoryMapToCategoryDTO(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());

        List<SubCategoryDTO> subCategoryDTOList = new ArrayList<>();
        for (SubCategory subCategory : category.getSubCategories()) {
            SubCategoryDTO subCategoryDTO = new SubCategoryDTO();
            subCategoryDTO.setId(subCategory.getId());
            subCategoryDTO.setName(subCategory.getName());
            subCategoryDTOList.add(subCategoryDTO);
        }
        categoryDTO.setSubCategories(subCategoryDTOList);
        return categoryDTO;
    }

    public static List<CategoryDTO> categoryListMapToCategoryListDTO(List<Category> categoryList) {
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        for (Category category : categoryList) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setName(category.getName());
            categoryDTO.setId(category.getId());
            categoryDTO.setSubCategories(CategoryMapper.subCategoryListMapToSubCategoryListDTO(category.getSubCategories()));
            categoryDTOList.add(categoryDTO);
        }
        return categoryDTOList;
    }

    public static List<SubCategoryDTO> subCategoryListMapToSubCategoryListDTO(List<SubCategory> subCategoryList) {
        List<SubCategoryDTO> subCategoryDTOList = new ArrayList<>();
        for (SubCategory subCategory : subCategoryList) {
            SubCategoryDTO subCategoryDTO = new SubCategoryDTO();
            subCategoryDTO.setId(subCategory.getId());
            subCategoryDTO.setName(subCategory.getName());
            subCategoryDTOList.add(subCategoryDTO);
        }
        return subCategoryDTOList;
    }

}
