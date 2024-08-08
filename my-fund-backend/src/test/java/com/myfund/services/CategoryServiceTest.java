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

        User mockUser = User.builder().id(1L).build();


        Category category1 = Category.builder()
                .name("Category 1")
                .id(1L)
                .subCategories(new ArrayList<>())
                .build();

        Category category2 = Category.builder()
                .name("Category 2")
                .id(2L)
                .subCategories(new ArrayList<>())
                .build();

        SubCategory subCategory1 = SubCategory.builder().id(1L).name("SubCategory1").build();

        category1.setSubCategories(Arrays.asList(subCategory1));

        SubCategory subCategory2 = SubCategory.builder().id(2L).name("SubCategory2").build();

        category2.setSubCategories(Arrays.asList(subCategory2));

        List<Category> categories = Arrays.asList(category1, category2);
        when(categoryRepository.findAllCategoriesByUser(mockUser)).thenReturn(categories);
        List<Category> result = categoryService.findAllCategoriesByUser(mockUser);

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
        User user = User.builder().build();
        Category category = Category.builder()
                .id(categoryId)
                .name("Test Category")
                .user(user)
                .subCategories(Arrays.asList(SubCategory.builder().build()))
                .build();

        CategoryDTO categoryDTO = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .subCategories(Arrays.asList(SubCategoryDTO.builder().build())).build();

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        Category result = categoryService.findCategoryByIdAndUser(categoryId, user);


        assertEquals(categoryDTO.getId(), result.getId(), "Category ID should match");
        assertEquals(categoryDTO.getName(), result.getName(), "Category name should match");

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void findCategoryByIdAndUser_WhenCategoryDoesNotExist() {

        Long categoryId = 1L;
        User user = User.builder().build();
        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.findCategoryByIdAndUser(categoryId, user);
        }, "Category not found");

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void createCategory_NewCategory() throws InvalidInputException {

        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .name("New Category")
                .user(user)
                .subCategories(new ArrayList<>())
                .build();

        when(categoryRepository.findByNameAndUser(category.getName(), user)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.createCategory(category, user);

        assertNotNull(result, "The result should not be null");
        assertEquals(category.getName(), result.getName(), "The name of the category does not match");

        verify(categoryRepository, times(1)).findByNameAndUser(category.getName(), user);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_ExistingCategory() {

        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .name("Existing Category")
                .user(user)
                .subCategories(Arrays.asList(SubCategory.builder().build()))
                .build();

        Category existingCategory = Category.builder()
                .id(1L)
                .name(category.getName())
                .user(user)
                .subCategories(Arrays.asList(SubCategory.builder().build()))
                .build();

        when(categoryRepository.findByNameAndUser(category.getName(), user)).thenReturn(Optional.of(existingCategory));

        assertThrows(CategoryNotUniqueException.class, () -> categoryService.createCategory(category, user),
                "Category already exists for this user");

        verify(categoryRepository, times(1)).findByNameAndUser(category.getName(), user);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryExists() {

        Long categoryId = 1L;
        User user = User.builder().id(1L).build();

        SubCategory subCategory = SubCategory.builder().id(1L).name("test subcategory").build();

        Category existingCategory = Category.builder()
                .id(categoryId)
                .name("Old Category")
                .user(user)
                .subCategories(new ArrayList<>(Arrays.asList(subCategory)))
                .build();

        Category category = Category.builder()
                .name("Updated Category")
                .subCategories(Arrays.asList(subCategory))
                .build();

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category updatedCategory = categoryService.updateCategory(categoryId, category, user);

        assertNotNull(updatedCategory, "Updated category should not be null");
        assertEquals("Updated Category", updatedCategory.getName(), "Category name should be updated");
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryDoesNotExist() {

        Long categoryId = 1L;
        User user = User.builder().id(1L).build();


        Category category = Category.builder()
                .name("Nonexistent Category")
                .build();

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.updateCategory(categoryId, category, user);
        }, "Category not found");

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategoryByIdAndUser_WhenCategoryExists() {
        Long categoryId = 1L;
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .id(categoryId)
                .build();

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));
        categoryService.deleteCategoryByIdAndUser(categoryId, user);

        verify(categoryRepository, times(1)).delete(category);
        verify(budgetService, times(1)).updateExpensesCategoryIdToNull(categoryId);
        verify(budgetService, times(1)).updateIncomesCategoryIdToNull(categoryId);
    }

    @Test
    void deleteCategoryByIdAndUser_WhenCategoryDoesNotExist_ShouldThrowException() {

        Long categoryId = 1L;
        User user = User.builder().id(1L).build();

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.deleteCategoryByIdAndUser(categoryId, user),
                "Category not found");

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void isSubcategoryRelatedToCategory_WhenCategoryExistsAndSubcategoryIsRelated() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .id(categoryId)
                .build();

        SubCategory subCategory = SubCategory.builder().id(subcategoryId).name("Test Subcategory").build();
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
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .id(categoryId)
                .build();

        SubCategory subCategory = SubCategory.builder().id(1L).name("Test Subcategory").build();
        category.setSubCategories(List.of(subCategory));

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        boolean result = categoryService.isSubcategoryRelatedToCategory(subcategoryId, categoryId, user);

        assertFalse(result, "Subcategory should not be related to the category");
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void isSubcategoryRelatedToCategory_WhenSubcategoryListIsEmpty() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .id(categoryId)
                .user(user)
                .subCategories(new ArrayList<>())
                .build();

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        boolean result = categoryService.isSubcategoryRelatedToCategory(subcategoryId, categoryId, user);

        assertFalse(result, "Subcategory relation cannot be established if subcategory list is empty");
        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
    }

    @Test
    void deleteSubcategoryByIdsAndUser_Success() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .user(user)
                .id(categoryId)
                .subCategories(new ArrayList<>())
                .build();

        SubCategory subCategory = SubCategory.builder().id(subcategoryId).name("Test Subcategory").build();
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
        User user = User.builder().id(1L).build();

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
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .user(user)
                .id(categoryId)
                .subCategories(new ArrayList<>())
                .build();


        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        assertThrows(SubcategoryNotFoundException.class, () -> categoryService.deleteSubcategoryByIdsAndUser(categoryId, subcategoryId, user));

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(subCategoryRepository, never()).delete(any(SubCategory.class));
    }

    @Test
    void deleteSubcategoryByIdsAndUser_MultipleSubcategories() {
        Long categoryId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .user(user)
                .id(categoryId)
                .subCategories(new ArrayList<>())
                .build();

        SubCategory subCategory1 = SubCategory.builder().id(1L).name("Test Subcategory 1").build();
        SubCategory subCategory2 = SubCategory.builder().id(2L).name("Test Subcategory 2").build();
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
        User user = User.builder().id(1L).build();

        Category category = Category.builder()
                .user(user)
                .id(categoryId)
                .subCategories(new ArrayList<>())
                .build();

        SubCategory subCategory = SubCategory.builder().id(2L).name("Test Subcategory").build();

        category.getSubCategories().add(subCategory);

        when(categoryRepository.findByIdAndUser(categoryId, user)).thenReturn(Optional.of(category));

        assertThrows(SubcategoryNotFoundException.class, () -> categoryService.deleteSubcategoryByIdsAndUser(categoryId, subcategoryId, user));

        verify(categoryRepository, times(1)).findByIdAndUser(categoryId, user);
        verify(subCategoryRepository, never()).delete(any(SubCategory.class));
    }


}