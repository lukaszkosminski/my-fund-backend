package com.myfund.services;

import com.myfund.exceptions.CategoryNotFoundException;
import com.myfund.exceptions.CategoryNotUniqueException;
import com.myfund.exceptions.InvalidInputException;
import com.myfund.exceptions.SubcategoryNotFoundException;
import com.myfund.models.Category;
import com.myfund.models.DTOs.CategoryDTO;
import com.myfund.models.DTOs.CreateCategoryDTO;
import com.myfund.models.DTOs.CreateSubCategoryDTO;
import com.myfund.models.DTOs.SubCategoryDTO;
import com.myfund.models.SubCategory;
import com.myfund.models.User;
import com.myfund.repositories.CategoryRepository;
import com.myfund.repositories.SubCategoryRepository;
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

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private BudgetService budgetService;

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
    void createCategory_NewCategory() throws InvalidInputException {

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
        verify(budgetService, times(1)).updateExpensesCategoryIdToNull(categoryId);
        verify(budgetService, times(1)).updateIncomesCategoryIdToNull(categoryId);
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

    @Test
    void isSubcategoryRelatedToCategory_WhenCategoryExistsAndSubcategoryIsRelated() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);
        category.setUser(user);

        SubCategory subCategory = new SubCategory();
        subCategory.setId(subcategoryId);
        category.setSubCategories(List.of(subCategory));

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        boolean result = categoryService.isSubcategoryRelatedToCategory(subcategoryId, categoryId, user);

        assertTrue(result, "Subcategory should be related to the category");
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void isSubcategoryRelatedToCategory_WhenCategoryExistsAndSubcategoryIsNotRelated() {
        Long categoryId = 1L;
        Long subcategoryId = 2L;
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);
        category.setUser(user);

        SubCategory subCategory = new SubCategory();
        subCategory.setId(1L);
        category.setSubCategories(List.of(subCategory));

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        boolean result = categoryService.isSubcategoryRelatedToCategory(subcategoryId, categoryId, user);

        assertFalse(result, "Subcategory should not be related to the category");
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void isSubcategoryRelatedToCategory_WhenCategoryDoesNotExist() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.empty());

        boolean result = categoryService.isSubcategoryRelatedToCategory(subcategoryId, categoryId, user);

        assertFalse(result, "Subcategory relation cannot be established if category does not exist");
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void isSubcategoryRelatedToCategory_WhenUserIsNull() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;

        boolean result = categoryService.isSubcategoryRelatedToCategory(subcategoryId, categoryId, null);

        assertFalse(result, "Subcategory relation cannot be established if user is null");
        verify(categoryRepository, never()).findByIdAndUser(anyLong(), isNull());
    }

    @Test
    void isSubcategoryRelatedToCategory_WhenSubcategoryListIsEmpty() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);
        category.setUser(user);
        category.setSubCategories(new ArrayList<>());

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        boolean result = categoryService.isSubcategoryRelatedToCategory(subcategoryId, categoryId, user);

        assertFalse(result, "Subcategory relation cannot be established if subcategory list is empty");
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void deleteSubcategoryByIdsAndUser_Success() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);
        category.setUser(user);
        category.setSubCategories(new ArrayList<>());

        SubCategory subCategory = new SubCategory();
        subCategory.setId(subcategoryId);
        category.getSubCategories().add(subCategory);

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        categoryService.deleteSubcategoryByIdsAndUser(categoryId, subcategoryId, user);

        verify(budgetService, times(1)).updateExpensesSubcategoryIdToNull(subcategoryId);
        verify(budgetService, times(1)).updateIncomesSubcategoryIdToNull(subcategoryId);
        verify(subCategoryRepository, times(1)).delete(subCategory);
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void deleteSubcategoryByIdsAndUser_CategoryNotFound() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.deleteSubcategoryByIdsAndUser(categoryId, subcategoryId, user);
        });

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(subCategoryRepository, never()).delete(any(SubCategory.class));
    }

    @Test
    void deleteSubcategoryByIdsAndUser_SubcategoryNotFound() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);
        category.setUser(user);
        category.setSubCategories(new ArrayList<>());

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        assertThrows(SubcategoryNotFoundException.class, () -> categoryService.deleteSubcategoryByIdsAndUser(categoryId, subcategoryId, user));

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(subCategoryRepository, never()).delete(any(SubCategory.class));
    }

    @Test
    void deleteSubcategoryByIdsAndUser_MultipleSubcategories() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);
        category.setUser(user);
        category.setSubCategories(new ArrayList<>());

        SubCategory subCategory1 = new SubCategory();
        subCategory1.setId(subcategoryId);
        SubCategory subCategory2 = new SubCategory();
        subCategory2.setId(2L);
        category.getSubCategories().add(subCategory1);
        category.getSubCategories().add(subCategory2);

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        categoryService.deleteSubcategoryByIdsAndUser(categoryId, subcategoryId, user);

        verify(budgetService, times(1)).updateExpensesSubcategoryIdToNull(subcategoryId);
        verify(budgetService, times(1)).updateIncomesSubcategoryIdToNull(subcategoryId);
        verify(subCategoryRepository, times(1)).delete(subCategory1);
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void deleteSubcategoryByIdsAndUser_SubcategoryNotBelongToCategory() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(categoryId);
        category.setUser(user);
        category.setSubCategories(new ArrayList<>());

        SubCategory subCategory = new SubCategory();
        subCategory.setId(2L); // Different ID
        category.getSubCategories().add(subCategory);

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        assertThrows(SubcategoryNotFoundException.class, () -> categoryService.deleteSubcategoryByIdsAndUser(categoryId, subcategoryId, user));

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(subCategoryRepository, never()).delete(any(SubCategory.class));
    }


}