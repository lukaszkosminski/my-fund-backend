package com.myfund.services;

import com.myfund.exceptions.CategoryNotFoundException;
import com.myfund.exceptions.CategoryNotUniqueException;
import com.myfund.models.Category;
import com.myfund.models.DTOs.CategoryDTO;
import com.myfund.models.DTOs.CreateCategoryDTO;
import com.myfund.models.DTOs.CreateSubCategoryDTO;
import com.myfund.models.DTOs.SubCategoryDTO;
import com.myfund.models.SubCategory;
import com.myfund.models.User;
import com.myfund.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllCategoriesByUser() {

        User mockUser = new User();
        mockUser.setId(1L);

        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Category1");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Category2");

        SubCategory subCategory1 = new SubCategory();
        subCategory1.setId(1L);
        subCategory1.setName("SubCategory1");
        category1.setSubCategories(Arrays.asList(subCategory1));

        SubCategory subCategory2 = new SubCategory();
        subCategory2.setId(2L);
        subCategory2.setName("SubCategory2");

        category2.setSubCategories(Arrays.asList(subCategory2));

        List<Category> categories = Arrays.asList(category1, category2);
        when(categoryRepository.findAllCategoriesByUser(mockUser)).thenReturn(categories);
        List<CategoryDTO> result = categoryService.findAllCategoriesByUser(mockUser);

        assertNotNull(result, "The result should not be null");
        assertEquals(2, result.size(), "The size of the result list does not match the expected value");
        assertEquals(category1.getId(), result.get(0).getId(), "The ID of the first category does not match");
        assertEquals(category1.getName(), result.get(0).getName(), "The name of the first category does not match");
        assertEquals(category2.getId(), result.get(1).getId(), "The ID of the second category does not match");
        assertEquals(category2.getName(), result.get(1).getName(), "The name of the second category does not match");

        verify(categoryRepository, times(1)).findAllCategoriesByUser(mockUser);
    }

    @Test
    void findCategoryByIdAndUser_WhenCategoryExists() {

        Long categoryId = 1L;
        User user = new User();
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Test Category");
        category.setUser(user);
        category.setSubCategories(Arrays.asList(new SubCategory()));

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        categoryDTO.setSubCategories(Arrays.asList(new SubCategoryDTO()));

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        CategoryDTO result = categoryService.findCategoryByIdAndUser(categoryId, user);


        assertEquals(categoryDTO.getId(), result.getId(), "Category ID should match");
        assertEquals(categoryDTO.getName(), result.getName(), "Category name should match");

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void findCategoryByIdAndUser_WhenCategoryDoesNotExist() {

        Long categoryId = 1L;
        User user = new User();

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.findCategoryByIdAndUser(categoryId, user);
        }, "Category not found");

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void createCategory_NewCategory() {

        User user = new User();
        user.setId(1L);

        CreateCategoryDTO createCategoryDTO = new CreateCategoryDTO();
        createCategoryDTO.setName("New Category");

        when(categoryRepository.findByNameAndUser(createCategoryDTO.getName(), user)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDTO result = categoryService.createCategory(createCategoryDTO, user);

        assertNotNull(result, "The result should not be null");
        assertEquals(createCategoryDTO.getName(), result.getName(), "The name of the category does not match");

        verify(categoryRepository, times(1)).findByNameAndUser(createCategoryDTO.getName(), user);
        verify(categoryRepository, times(2)).save(any(Category.class));
    }

    @Test
    void createCategory_ExistingCategory() {

        User user = new User();
        user.setId(1L);

        CreateCategoryDTO createCategoryDTO = new CreateCategoryDTO();
        createCategoryDTO.setName("Existing Category");
        createCategoryDTO.setSubCategories(Arrays.asList(new CreateSubCategoryDTO()));

        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName(createCategoryDTO.getName());
        existingCategory.setUser(user);
        existingCategory.setSubCategories(Arrays.asList(new SubCategory()));

        when(categoryRepository.findByNameAndUser(createCategoryDTO.getName(), user)).thenReturn(Optional.of(existingCategory));

        assertThrows(CategoryNotUniqueException.class, () -> categoryService.createCategory(createCategoryDTO, user),
                "Category already exists for this user");

        verify(categoryRepository, times(1)).findByNameAndUser(createCategoryDTO.getName(), user);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryExists() {

        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);

        CreateSubCategoryDTO createSubCategoryDTO = new CreateSubCategoryDTO();
        createSubCategoryDTO.setName("CreateSubCategory");

        SubCategory subCategory = new SubCategory();
        subCategory.setId(1L);
        subCategory.setName(createSubCategoryDTO.getName());

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old Category");
        existingCategory.setUser(user);
        existingCategory.setSubCategories(new ArrayList<>(Arrays.asList(subCategory)));

        CreateCategoryDTO createCategoryDTO = new CreateCategoryDTO();
        createCategoryDTO.setName("Updated Category");
        createCategoryDTO.setSubCategories(Arrays.asList(createSubCategoryDTO));

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, createCategoryDTO, user);

        assertNotNull(updatedCategory, "Updated category should not be null");
        assertEquals("Updated Category", updatedCategory.getName(), "Category name should be updated");
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryDoesNotExist() {

        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);

        CreateCategoryDTO createCategoryDTO = new CreateCategoryDTO();
        createCategoryDTO.setName("Nonexistent Category");

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.updateCategory(categoryId, createCategoryDTO, user);
        }, "Category not found");

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategoryByIdAndUser_WhenCategoryExists() {

        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));
        categoryService.deleteCategoryByIdAndUser(categoryId, user);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void deleteCategoryByIdAndUser_WhenCategoryDoesNotExist_ShouldThrowException() {

        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.deleteCategoryByIdAndUser(categoryId, user),
                "Category not found");

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}