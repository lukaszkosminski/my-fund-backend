package com.myfund.model.DTO.mappers;

import com.myfund.model.Category;
import com.myfund.model.DTO.CategoryDTO;
import com.myfund.model.DTO.CreateCategoryDTO;
import com.myfund.model.DTO.CreateSubCategoryDTO;
import com.myfund.model.DTO.SubCategoryDTO;
import com.myfund.model.SubCategory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    @Test
    void testCreateCategoryDTOMapToCategory() {

        CreateSubCategoryDTO subCategoryDTO = new CreateSubCategoryDTO();
        subCategoryDTO.setName("SubCategory1");

        CreateCategoryDTO categoryDTO = new CreateCategoryDTO();
        categoryDTO.setName("Category1");
        categoryDTO.setSubCategories(Arrays.asList(subCategoryDTO));

        Category category = CategoryMapper.createCategoryDTOMapToCategory(categoryDTO);

        assertEquals("Category1", category.getName(), "Category name does not match");
        assertTrue(category.getSubCategories().size() == 1, "SubCategory list size does not match");
        assertEquals("SubCategory1", category.getSubCategories().get(0).getName(), "SubCategory name does not match");
    }

    @Test
    void testCategoryMapToCategoryDTO() {

        SubCategory subCategory = new SubCategory();
        subCategory.setId(1L);
        subCategory.setName("SubCategory1");

        Category category = new Category();
        category.setId(1L);
        category.setName("Category1");
        category.setSubCategories(Arrays.asList(subCategory));

        CategoryDTO categoryDTO = CategoryMapper.categoryMapToCategoryDTO(category);

        assertNotNull(categoryDTO, "CategoryDTO should not be null");
        assertEquals(category.getId(), categoryDTO.getId(), "Category ID does not match");
        assertEquals(category.getName(), categoryDTO.getName(), "Category name does not match");
        assertNotNull(categoryDTO.getSubCategories(), "SubCategories list should not be null");
        assertEquals(1, categoryDTO.getSubCategories().size(), "SubCategories list size does not match");
        assertEquals(subCategory.getId(), categoryDTO.getSubCategories().get(0).getId(), "SubCategory ID does not match");
        assertEquals(subCategory.getName(), categoryDTO.getSubCategories().get(0).getName(), "SubCategory name does not match");
    }
    @Test
    void testCategoryListMapToCategoryListDTO() {

        SubCategory subCategory1 = new SubCategory();
        subCategory1.setId(1L);
        subCategory1.setName("SubCategory1");

        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Category1");
        category1.setSubCategories(Arrays.asList(subCategory1));

        List<Category> categories = Arrays.asList(category1);

        List<CategoryDTO> categoryDTOs = CategoryMapper.categoryListMapToCategoryListDTO(categories);
        CategoryDTO categoryDTO = categoryDTOs.get(0);

        assertNotNull(categoryDTOs, "The returned list should not be null");
        assertEquals(1, categoryDTOs.size(), "The size of the returned list does not match the expected value");
        assertEquals(category1.getId(), categoryDTO.getId(), "Category ID does not match");
        assertEquals(category1.getName(), categoryDTO.getName(), "Category name does not match");
        assertNotNull(categoryDTO.getSubCategories(), "SubCategories list should not be null");
        assertEquals(1, categoryDTO.getSubCategories().size(), "SubCategories list size does not match");
        assertEquals(subCategory1.getId(), categoryDTO.getSubCategories().get(0).getId(), "SubCategory ID does not match");
        assertEquals(subCategory1.getName(), categoryDTO.getSubCategories().get(0).getName(), "SubCategory name does not match");
    }

    @Test
    void testSubCategoryListMapToSubCategoryListDTO() {

        SubCategory subCategory1 = new SubCategory();
        subCategory1.setId(1L);
        subCategory1.setName("SubCategory1");

        SubCategory subCategory2 = new SubCategory();
        subCategory2.setId(2L);
        subCategory2.setName("SubCategory2");

        List<SubCategory> subCategoryList = Arrays.asList(subCategory1, subCategory2);
        List<SubCategoryDTO> subCategoryDTOList = CategoryMapper.subCategoryListMapToSubCategoryListDTO(subCategoryList);

        assertNotNull(subCategoryDTOList, "The returned list should not be null");
        assertEquals(2, subCategoryDTOList.size(), "The size of the returned list does not match the expected value");

        SubCategoryDTO subCategoryDTO1 = subCategoryDTOList.get(0);
        assertEquals(subCategory1.getId(), subCategoryDTO1.getId(), "SubCategory ID does not match for the first subcategory");
        assertEquals(subCategory1.getName(), subCategoryDTO1.getName(), "SubCategory name does not match for the first subcategory");

        SubCategoryDTO subCategoryDTO2 = subCategoryDTOList.get(1);
        assertEquals(subCategory2.getId(), subCategoryDTO2.getId(), "SubCategory ID does not match for the second subcategory");
        assertEquals(subCategory2.getName(), subCategoryDTO2.getName(), "SubCategory name does not match for the second subcategory");
    }
}