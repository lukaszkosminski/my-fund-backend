package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateSubCategoryDTO;
import com.myfund.models.SubCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubCategoryMapperTest {

    @Test
    void createSubCategoryMapToSubcategory_ShouldMapNameCorrectly() {
        CreateSubCategoryDTO createSubCategoryDTO = new CreateSubCategoryDTO();
        createSubCategoryDTO.setName("Test SubCategory");

        SubCategory subCategory = SubCategoryMapper.toModel(createSubCategoryDTO);

        assertNotNull(subCategory, "The mapped SubCategory should not be null");
        assertEquals("Test SubCategory", subCategory.getName(), "The name of the SubCategory does not match the expected value");
    }

    @Test
    void createSubCategoryMapToSubcategory_ShouldHandleNullName() {
        CreateSubCategoryDTO createSubCategoryDTO = new CreateSubCategoryDTO();
        createSubCategoryDTO.setName(null);

        SubCategory subCategory = SubCategoryMapper.toModel(createSubCategoryDTO);

        assertNotNull(subCategory, "The mapped SubCategory should not be null");
        assertNull(subCategory.getName(), "The name of the SubCategory should be null");
    }

    @Test
    void createSubCategoryMapToSubcategory_ShouldHandleEmptyName() {
        CreateSubCategoryDTO createSubCategoryDTO = new CreateSubCategoryDTO();
        createSubCategoryDTO.setName("");

        SubCategory subCategory = SubCategoryMapper.toModel(createSubCategoryDTO);

        assertNotNull(subCategory, "The mapped SubCategory should not be null");
        assertEquals("", subCategory.getName(), "The name of the SubCategory should be an empty string");
    }

    @Test
    void createSubCategoryMapToSubcategory_ShouldHandleWhitespaceName() {
        CreateSubCategoryDTO createSubCategoryDTO = new CreateSubCategoryDTO();
        createSubCategoryDTO.setName("   ");

        SubCategory subCategory = SubCategoryMapper.toModel(createSubCategoryDTO);

        assertNotNull(subCategory, "The mapped SubCategory should not be null");
        assertEquals("   ", subCategory.getName(), "The name of the SubCategory should be whitespace");
    }

    @Test
    void createSubCategoryMapToSubcategory_ShouldHandleSpecialCharactersInName() {
        CreateSubCategoryDTO createSubCategoryDTO = new CreateSubCategoryDTO();
        createSubCategoryDTO.setName("!@#$%^&*()");

        SubCategory subCategory = SubCategoryMapper.toModel(createSubCategoryDTO);

        assertNotNull(subCategory, "The mapped SubCategory should not be null");
        assertEquals("!@#$%^&*()", subCategory.getName(), "The name of the SubCategory should match the special characters");
    }
}