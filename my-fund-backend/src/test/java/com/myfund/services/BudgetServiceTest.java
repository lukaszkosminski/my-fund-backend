package com.myfund.services;

import com.myfund.exceptions.*;
import com.myfund.models.*;
import com.myfund.models.DTOs.*;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.repositories.BudgetRepository;
import com.myfund.repositories.ExpenseRepository;
import com.myfund.repositories.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BudgetServiceTest {

    @InjectMocks
    private BudgetService budgetService;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Captor
    private ArgumentCaptor<Budget> budgetArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDefaultBudget() {
        User user = User.builder().email("test@example.com").build();

        budgetService.createDefaultBudget(user);

        verify(budgetRepository).save(budgetArgumentCaptor.capture());
        Budget capturedBudget = budgetArgumentCaptor.getValue();

        assertEquals("Default Budget", capturedBudget.getName());
        assertEquals(BigDecimal.ZERO, capturedBudget.getBalance());
        assertEquals(BigDecimal.ZERO, capturedBudget.getTotalExpense());
        assertEquals(BigDecimal.ZERO, capturedBudget.getTotalIncome());
        assertEquals(user, capturedBudget.getUser());

        assertTrue(capturedBudget.getLocalDateTime().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void createBudget_NewBudget_Success() throws InvalidInputException {

        Budget budget = Budget.builder()
                .name("Test Budget")
                .build();
        User user = User.builder().email("test@example.com").build();

        when(budgetRepository.findByNameAndUser(anyString(), any(User.class))).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Budget result = budgetService.createBudget(budget, user);

        assertNotNull(result);
        assertEquals("Test Budget", result.getName());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void createBudget_DuplicateBudget_ThrowsException() {


        Budget budget = Budget.builder()
                .name("Test Budget")
                .build();

        User user = User.builder().email("test@example.com").build();

        Budget existingBudget = Budget.builder()
                .name("Existing Budget")
                .user(user)
                .localDateTime(LocalDateTime.now())
                .build();

        when(budgetRepository.findByNameAndUser(anyString(), any(User.class))).thenReturn(Optional.of(existingBudget));

        assertThrows(BudgetNotUniqueException.class, () -> budgetService.createBudget(budget, user));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void findAllBudgetsByUser_ReturnsBudgetSummaryList() {

        User user = User.builder().id(1L).build();

        Budget budget1 = Budget.builder().build();
        Budget budget2 = Budget.builder().build();
        List<Budget> budgets = Arrays.asList(budget1, budget2);

        when(budgetRepository.findAllByUser(user)).thenReturn(budgets);

        List<Budget> allBudgetsByUser = budgetService.findAllBudgetsByUser(user);

        assertNotNull(allBudgetsByUser, "The result should not be null");
        assertEquals(2, allBudgetsByUser.size(), "The result list should contain two budget summaries");
        verify(budgetRepository).findAllByUser(user);
        verifyNoMoreInteractions(budgetRepository);
    }

    @Test
    void findBudgetByIdAndUser_WhenBudgetExists() {

        Budget budget = Budget.builder().build();
        User user = User.builder().build();
        when(budgetRepository.findByIdAndUser(any(Long.class), any(User.class))).thenReturn(Optional.of(budget));

        Budget result = budgetService.findBudgetByIdAndUser(1L, user);

        assertNotNull(result);

        verify(budgetRepository).findByIdAndUser(1L, user);
    }

    @Test
    void findBudgetByIdAndUser_WhenBudgetDoesNotExist() {

        User user = User.builder().build();
        when(budgetRepository.findByIdAndUser(any(Long.class), any(User.class))).thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class, () -> budgetService.findBudgetByIdAndUser(1L, user));

        verify(budgetRepository).findByIdAndUser(1L, user);
    }

    @Test
    void createExpenseWhenBudgetExistsAndCategoryIsValid() throws InvalidInputException {

        Budget budget = Budget.builder()
                .id(1L)
                .totalIncome(new BigDecimal("100"))
                .build();
        User user = User.builder().build();
        Expense expense = Expense.builder()
                .idCategory(1L)
                .idSubCategory(2L)
                .amount(new BigDecimal("100"))
                .name("Test Expense")
                .build();

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(budget));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(true);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = budgetService.createExpense(1L, expense, user);

        assertNotNull(result);
        verify(expenseRepository).save(any(Expense.class));
        verify(budgetRepository).findByIdAndUser(anyLong(), any(User.class));
    }

    @Test
    void createExpenseWhenBudgetExistsButCategoryIsInvalid() {

        Budget budget = Budget.builder()
                .id(1L)
                .totalIncome(new BigDecimal("100"))
                .build();
        Expense expense = Expense.builder()
                .idCategory(1L)
                .idSubCategory(2L)
                .amount(new BigDecimal("100"))
                .name("Test Expense")
                .build();
        User user = User.builder().build();

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(budget));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(false);

        assertThrows(SubcategoryNotRelatedToCategoryException.class, () -> budgetService.createExpense(1L, expense, user));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpenseWhenBudgetDoesNotExist() {

        Expense expense = Expense.builder()
                .build();
        User user = User.builder().build();
        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.empty());

        assertThrows(InvalidInputException.class, () -> budgetService.createExpense(1L, expense, user));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createIncome_WhenBudgetExistsAndCategoryIsValid() throws InvalidInputException {

        Budget budget = Budget.builder()
                .id(1L)
                .totalIncome(new BigDecimal("100"))
                .totalExpense(new BigDecimal("100"))
                .build();
        User user = User.builder().build();
        Income income = Income.builder()
                .idCategory(1L)
                .idSubCategory(2L)
                .amount(new BigDecimal("100"))
                .name("Test Income")
                .build();

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(budget));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(true);
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Income result = budgetService.createIncome(1L, income, user);

        assertNotNull(result);
        verify(incomeRepository).save(any(Income.class));
        verify(budgetRepository).findByIdAndUser(anyLong(), any(User.class));
    }

    @Test
    void createIncome_WhenBudgetDoesNotExist() {
        Income income = Income.builder()
                .build();
        User user = User.builder().build();

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.empty());

        assertThrows(InvalidInputException.class, () -> budgetService.createIncome(1L, income, user));

        verify(incomeRepository, never()).save(any(Income.class));
    }

    @Test
    void createIncome_WhenBudgetExistsButCategoryIsInvalid() {
        Budget budget = Budget.builder().build();
        Income income = Income.builder()
                .idCategory(1L)
                .idSubCategory(2L)
                .build();
        User user = User.builder().build();

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(budget));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(false);

        assertThrows(InvalidInputException.class, () -> budgetService.createIncome(1L, income, user));

        verify(incomeRepository, never()).save(any(Income.class));
    }

    @Test
    void updateExpense_ExpenseFoundAndUpdated() throws InvalidInputException {

        User user = User.builder().id(1L).build();

        Expense expense = Expense.builder()
                .id(1L)
                .user(user)
                .build();

        Expense newExpense = Expense.builder()
                .idCategory(1L)
                .idSubCategory(2L)
                .amount(new BigDecimal("200"))
                .name("Test Expense")
                .build();

        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(true);
        when(expenseRepository.findByIdAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = budgetService.updateExpense(1L, 1L, newExpense, user);

        assertNotNull(result);
        assertEquals(newExpense.getAmount(), result.getAmount());
        assertEquals(newExpense.getName(), result.getName());
    }

    @Test
    void updateExpense_ExpenseNotFound() {

        User user = User.builder().id(1L).build();

        Expense newExpense = Expense.builder()
                .idCategory(1L)
                .idSubCategory(2L)
                .amount(new BigDecimal("200"))
                .name("Test Expense")
                .build();

        when(expenseRepository.findByIdAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () -> budgetService.updateExpense(1L, 1L, newExpense, user));
    }

    @Test
    void updateExpense_SubcategoryNotRelated() {

        User user = User.builder().id(1L).build();

        Expense expense = Expense.builder()
                .id(1L)
                .user(user)
                .build();

        Expense newExpense = Expense.builder()
                .idCategory(1L)
                .idSubCategory(2L)
                .amount(new BigDecimal("200"))
                .name("Test Expense")
                .build();

        when(expenseRepository.findByIdAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(expense));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenThrow(new SubcategoryNotRelatedToCategoryException("Subcategory not related"));

        assertThrows(SubcategoryNotRelatedToCategoryException.class, () -> budgetService.updateExpense(1L, 1L, newExpense, user));
    }

    @Test
    void updateIncome_Success() throws InvalidInputException {

        User user = User.builder().id(1L).build();
        Income newIncome = Income.builder()
                .idCategory(1L)
                .idSubCategory(2L)
                .amount(new BigDecimal("200"))
                .name("Test Expense")
                .build();

        Income income = Income.builder()
                .id(1L)
                .user(user)
                .build();

        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(true);
        when(incomeRepository.findByIdAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(income));
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Income result = budgetService.updateIncome(1L, 1L, newIncome, user);

        assertNotNull(result);
        assertEquals(newIncome.getAmount(), result.getAmount());
        assertEquals(newIncome.getName(), result.getName());
    }

    @Test
    void updateIncome_IncomeNotFound() {
        User user = User.builder().id(1L).build();
        Income newIncome = Income.builder().build();
        Long budgetId = 1L;
        Long incomeId = 1L;

        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.empty());

        assertThrows(InvalidInputException.class, () -> budgetService.updateIncome(budgetId, incomeId, newIncome, user));
    }

    @Test
    void updateIncome_SubcategoryNotRelatedToCategory() {

        User user = User.builder().id(1L).build();
        Income newIncome = Income.builder()
                .idCategory(1L)
                .idSubCategory(1L)
                .build();
        Income income = Income.builder()
                .id(1L)
                .build();
        Long budgetId = 1L;
        Long incomeId = 1L;

        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.of(income));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenThrow(new SubcategoryNotRelatedToCategoryException("Subcategory not related"));

        assertThrows(InvalidInputException.class, () -> budgetService.updateIncome(budgetId, incomeId, newIncome, user));
    }

    @Test
    void validateCategoryAndSubCategory_BothNull() {
        User user = User.builder().id(1L).build();
        assertTrue(budgetService.validateCategoryAndSubCategory(null, null, user));
    }

    @Test
    void validateCategoryAndSubCategory_CategoryNotNullSubCategoryNull() {
        User user = User.builder().id(1L).build();
        assertTrue(budgetService.validateCategoryAndSubCategory(1L, null, user));
    }

    @Test
    void validateCategoryAndSubCategory_CategoryNullSubCategoryNotNull_ThrowsException() {
        User user = User.builder().id(1L).build();
        assertThrows(SubcategoryNotFoundException.class, () -> budgetService.validateCategoryAndSubCategory(null, 1L, user));
    }

    @Test
    void validateCategoryAndSubCategory_BothNotNullAndRelated() {
        User user = User.builder().id(1L).build();
        when(categoryService.isSubcategoryRelatedToCategory(1L, 1L, user)).thenReturn(true);
        assertTrue(budgetService.validateCategoryAndSubCategory(1L, 1L, user));
        verify(categoryService).isSubcategoryRelatedToCategory(1L, 1L, user);
    }

    @Test
    void validateCategoryAndSubCategory_BothNotNullAndNotRelated() {
        User user = User.builder().id(1L).build();
        when(categoryService.isSubcategoryRelatedToCategory(2L, 1L, user)).thenReturn(false);
        assertFalse(budgetService.validateCategoryAndSubCategory(1L, 2L, user));
        verify(categoryService).isSubcategoryRelatedToCategory(2L, 1L, user);
    }

    @Test
    void updateExpensesCategoryIdToNull() {

        Long categoryId = 1L;
        List<Expense> expenses = Arrays.asList(
                Expense.builder().build(),
                Expense.builder().build()
        );
        when(expenseRepository.findByIdCategory(categoryId)).thenReturn(expenses);

        budgetService.updateExpensesCategoryIdToNull(categoryId);

        verify(expenseRepository, times(1)).findByIdCategory(categoryId);
        for (Expense expense : expenses) {
            assertNull(expense.getIdCategory(), "Category ID should be null");
            assertNull(expense.getIdSubCategory(), "Subcategory ID should be null");
        }
        verify(expenseRepository, times(1)).saveAll(expenses);
    }

    @Test
    void updateIncomesCategoryIdToNull() {

        Long idCategory = 1L;
        List<Income> incomes = Arrays.asList(
                Income.builder().build(),
                Income.builder().build()
        );
        when(incomeRepository.findByIdCategory(idCategory)).thenReturn(incomes);

        budgetService.updateIncomesCategoryIdToNull(idCategory);

        verify(incomeRepository, times(1)).findByIdCategory(idCategory);
        for (Income income : incomes) {
            assertNull(income.getIdCategory(), "Category ID should be null");
            assertNull(income.getIdSubCategory(), "Subcategory ID should be null");
        }
    }

    @Test
    void updateExpensesSubcategoryIdToNull() {

        Long subcategoryId = 1L;
        List<Expense> expenses = Arrays.asList(
                Expense.builder().build(),
                Expense.builder().build()
        );
        when(expenseRepository.findByIdSubCategory(subcategoryId)).thenReturn(expenses);

        budgetService.updateExpensesSubcategoryIdToNull(subcategoryId);

        verify(expenseRepository, times(1)).findByIdSubCategory(subcategoryId);
        expenses.forEach(expense -> assertNull(expense.getIdSubCategory()));
    }

    @Test
    void updateIncomesSubcategoryIdToNull() {

        Long subcategoryId = 1L;
        List<Income> incomes = Arrays.asList(
                Income.builder().build(),
                Income.builder().build()
        );
        incomes.forEach(income -> income.setIdSubCategory(subcategoryId));

        when(incomeRepository.findByIdSubCategory(subcategoryId)).thenReturn(incomes);

        budgetService.updateIncomesSubcategoryIdToNull(subcategoryId);

        verify(incomeRepository, times(1)).findByIdSubCategory(subcategoryId);
        incomes.forEach(income -> assertNull(income.getIdSubCategory()));

    }

    @Test
    void getTotalExpensesByCategory_ReturnsCorrectValue() {

        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = User.builder().id(1L).build();
        BigDecimal expectedTotalExpenses = new BigDecimal("100.00");

        Expense expense = Expense.builder()
                .amount(expectedTotalExpenses)
                .build();
        List<Expense> expenses = Arrays.asList(expense);

        when(expenseRepository.findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId())).thenReturn(expenses);

        FinancialAggregate result = budgetService.getTotalExpensesByCategory(budgetId, categoryId, user);

        assertNotNull(result);
        verify(expenseRepository, times(1)).findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId());
        assertEquals(expectedTotalExpenses, result.getValue());

    }

    @Test
    void getTotalExpensesByCategory_NoExpensesFound() {
        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = User.builder().id(1L).build();
        when(expenseRepository.findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId())).thenReturn(null);

        FinancialAggregate result = budgetService.getTotalExpensesByCategory(budgetId, categoryId, user);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getValue());
    }

    @Test
    void getTotalExpensesByCategory_ExceptionThrown() {
        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = User.builder().id(1L).build();
        when(expenseRepository.findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId())).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(TotalExpensesRetrievalException.class, () -> {
            budgetService.getTotalExpensesByCategory(budgetId, categoryId, user);
        });

        String expectedMessage = "Error retrieving total expenses for budget ID: " + budgetId + ", category ID: " + categoryId + ", and user ID: " + user.getId();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getTotalExpensesBySubcategory_ExpensesFound() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();
        BigDecimal expectedTotalExpenses = new BigDecimal("100.00");

        Expense expense = Expense.builder()
                .amount(expectedTotalExpenses)
                .build();
        List<Expense> expenses = Arrays.asList(expense);

        when(expenseRepository.findByIdSubCategoryAndUserIdAndBudgetId(budgetId, subcategoryId, user.getId())).thenReturn(expenses);
        FinancialAggregate result = budgetService.getTotalExpensesBySubcategory(budgetId, subcategoryId, user);

        assertNotNull(result);
        assertEquals(expectedTotalExpenses, result.getValue());
    }

    @Test
    void getTotalExpensesBySubcategory_NoExpensesFound() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();

        when(expenseRepository.findByIdSubCategoryAndUserIdAndBudgetId(budgetId, subcategoryId, user.getId())).thenReturn(null);

        FinancialAggregate result = budgetService.getTotalExpensesBySubcategory(budgetId, subcategoryId, user);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getValue());
    }

    @Test
    void getTotalExpensesBySubcategory_ThrowsException() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();

        when(expenseRepository.findByIdSubCategoryAndUserIdAndBudgetId(budgetId, subcategoryId, user.getId())).thenThrow(new RuntimeException("Database error"));


        assertThrows(TotalExpensesRetrievalException.class, () -> budgetService.getTotalExpensesBySubcategory(budgetId, subcategoryId, user));
    }

    @Test
    void getTotalIncomesByCategory_WithIncomes() {

        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = User.builder().id(1L).build();
        BigDecimal expectedTotalIncomes = new BigDecimal("1000");
        Income income1 = Income.builder()
                .amount(new BigDecimal("500"))
                .build();
        Income income2 = Income.builder()
                .amount(new BigDecimal("500"))
                .build();
        List<Income> incomes = Arrays.asList(income1, income2);

        when(incomeRepository.findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId())).thenReturn(incomes);


        FinancialAggregate result = budgetService.getTotalIncomesByCategory(budgetId, categoryId, user);


        assertNotNull(result);
        assertEquals(expectedTotalIncomes, result.getValue());
        verify(incomeRepository, times(1)).findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId());
    }

    @Test
    void getTotalIncomesByCategory_WithNoIncomes() {

        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = User.builder().id(1L).build();
        when(incomeRepository.findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId())).thenReturn(null);


        FinancialAggregate result = budgetService.getTotalIncomesByCategory(budgetId, categoryId, user);


        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getValue());
        verify(incomeRepository, times(1)).findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId());
    }

    @Test
    void getTotalIncomesByCategory_ThrowsException() {

        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = User.builder().id(1L).build();
        when(incomeRepository.findByIdCategoryAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenThrow(new RuntimeException("Database error"));

        assertThrows(TotalIncomesRetrievalException.class, () -> budgetService.getTotalIncomesByCategory(budgetId, categoryId, user));
        verify(incomeRepository, times(1)).findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId());
    }

    @Test
    void getTotalIncomesBySubcategoryReturnsCorrectValue() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();
        BigDecimal expectedTotalIncomes = new BigDecimal("1000");

        Income income1 = Income.builder()
                .amount(new BigDecimal("500"))
                .build();
        Income income2 = Income.builder()
                .amount(new BigDecimal("500"))
                .build();
        List<Income> incomes = Arrays.asList(income1, income2);

        when(incomeRepository.findByIdSubCategoryAndUserIdAndBudgetId(budgetId, subcategoryId, user.getId())).thenReturn(incomes);

        FinancialAggregate result = budgetService.getTotalIncomesBySubcategory(budgetId, subcategoryId, user);

        assertNotNull(result);
        assertEquals(expectedTotalIncomes, result.getValue());
    }

    @Test
    void getTotalIncomesBySubcategoryReturnsZeroWhenNoIncomesFound() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();
        when(incomeRepository.findByIdSubCategoryAndUserIdAndBudgetId(budgetId, subcategoryId, user.getId())).thenReturn(null);

        FinancialAggregate result = budgetService.getTotalIncomesBySubcategory(budgetId, subcategoryId, user);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getValue());
    }

    @Test
    void getTotalIncomesBySubcategoryThrowsExceptionOnFailure() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = User.builder().id(1L).build();
        when(incomeRepository.findByIdSubCategoryAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenThrow(new RuntimeException("Database error"));

        assertThrows(TotalIncomesRetrievalException.class, () -> budgetService.getTotalIncomesBySubcategory(budgetId, subcategoryId, user));
    }

    @Test
    void deleteBudgetByIdAndUser_WhenBudgetExists() {

        User testUser = User.builder().id(1L).build();
        Long testBudgetId = 2L;
        when(budgetRepository.existsByIdAndUserId(testBudgetId, testUser.getId())).thenReturn(true);

        budgetService.deleteBudgetByIdAndUser(testBudgetId, testUser);

        verify(budgetRepository).deleteBudgetByIdAndUser(testBudgetId, testUser);
        verify(expenseRepository).findByBudgetId(testBudgetId);
        verify(incomeRepository).findByBudgetId(testBudgetId);
    }

    @Test
    void deleteBudgetByIdAndUser_WhenBudgetDoesNotExist() {

        User testUser = User.builder().id(1L).build();
        Long testBudgetId = 2L;
        when(budgetRepository.existsByIdAndUserId(testBudgetId, testUser.getId())).thenReturn(false);

        assertThrows(BudgetNotFoundException.class, () -> budgetService.deleteBudgetByIdAndUser(testBudgetId, testUser));
    }

    @Test
    void deleteExpenseByIdAndUser_ExpenseExists() {

        Long expenseId = 1L;
        Long budgetId = 1L;
        User user = User.builder().id(1L).build();

        Expense expense = Expense.builder()
                .build();

        when(expenseRepository.findByIdAndUserIdAndBudgetId(expenseId, user.getId(), budgetId)).thenReturn(Optional.of(expense));

        budgetService.deleteExpenseByIdAndUser(expenseId, user, budgetId);

        verify(expenseRepository, times(1)).deleteExpenseByIdAndUserAndBudgetId(expenseId, user, budgetId);
    }

    @Test
    void deleteExpenseByIdAndUser_ExpenseDoesNotExist_ThrowsException() {

        Long expenseId = 1L;
        Long budgetId = 1L;
        User user = User.builder().id(1L).build();

        when(expenseRepository.findByIdAndUserIdAndBudgetId(expenseId, user.getId(), budgetId)).thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () -> budgetService.deleteExpenseByIdAndUser(expenseId, user, budgetId));

        verify(expenseRepository, never()).deleteExpenseByIdAndUserAndBudgetId(anyLong(), any(User.class), anyLong());
    }

    @Test
    void deleteIncomeByIdAndUser_WhenIncomeExists() {

        User user = User.builder().id(1L).build();
        Long incomeId = 2L;
        Long budgetId = 3L;

        Income income = Income.builder().build();
        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.of(income));

        budgetService.deleteIncomeByIdAndUser(incomeId, user, budgetId);

        verify(incomeRepository).deleteExpenseByIdAndUserAndBudgetId(incomeId, user, budgetId);
    }

    @Test
    void deleteIncomeByIdAndUser_WhenIncomeDoesNotExist_ThrowsException() {

        User user = User.builder().id(1L).build();
        Long incomeId = 2L;
        Long budgetId = 3L;

        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.empty());

        assertThrows(IncomeNotFoundException.class, () -> budgetService.deleteIncomeByIdAndUser(incomeId, user, budgetId));

        verify(incomeRepository, never()).deleteExpenseByIdAndUserAndBudgetId(any(), any(), any());
    }


    @Test
    public void testNoExpenses() {
        User user = User.builder().id(1L).build();
        Long budgetId = 1L;

        when(expenseRepository.findByBudgetIdAndUser(budgetId, user)).thenReturn(new ArrayList<>());
        ExpensesSummary expensesSummary = budgetService.calculateExpensesSummary(user, budgetId);
        assertTrue(expensesSummary.getExpensesSummary().isEmpty());
    }

    @Test
    public void testExpensesInOneCategory() {
        User user = User.builder().id(1L).build();
        Budget budget = Budget.builder()
                .id(1L)
                .build();

        Expense expense1 = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .budget(budget)
                .idCategory(1L)
                .idSubCategory(1L)
                .user(user)
                .build();

        Expense expense2 = Expense.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(200))
                .budget(budget)
                .idCategory(1L)
                .idSubCategory(1L)
                .user(user)
                .build();

        List<Expense> expenses = List.of(expense1, expense2);

        when(expenseRepository.findByBudgetIdAndUser(budget.getId(), user)).thenReturn(expenses);

        ExpensesSummary expensesSummary = budgetService.calculateExpensesSummary(user, budget.getId());
        assertEquals(1, expensesSummary.getExpensesSummary().size());
        assertEquals(BigDecimal.valueOf(300), expensesSummary.getExpensesSummary().get(0).getTotalExpenses());
        assertEquals(BigDecimal.valueOf(100), expensesSummary.getExpensesSummary().get(0).getSubcategories().get(0).getExpenseAmount());
        assertEquals(new BigDecimal("100.00"), expensesSummary.getExpensesSummary().get(0).getPercentageOfTotal());
    }

    @Test
    public void testExpensesInMultipleCategoriesWithPercentageOfTotal() {
        User user = User.builder().id(1L).build();
        Budget budget = Budget.builder()
                .id(1L)
                .build();

        Expense expense1 = Expense.builder()
                .id(1L)
                .amount(new BigDecimal("100"))
                .budget(budget)
                .idCategory(1L)
                .idSubCategory(1L)
                .user(user)
                .build();

        Expense expense2 = Expense.builder()
                .id(2L)
                .amount(new BigDecimal("200"))
                .budget(budget)
                .idCategory(1L)
                .idSubCategory(2L)
                .user(user)
                .build();

        Expense expense3 = Expense.builder()
                .id(3L)
                .amount(new BigDecimal("150"))
                .budget(budget)
                .idCategory(2L)
                .idSubCategory(3L)
                .user(user)
                .build();

        Expense expense4 = Expense.builder()
                .id(4L)
                .amount(new BigDecimal("250"))
                .budget(budget)
                .idCategory(2L)
                .idSubCategory(4L)
                .user(user)
                .build();

        List<Expense> expenses = List.of(expense1, expense2, expense3, expense4);

        when(expenseRepository.findByBudgetIdAndUser(budget.getId(), user)).thenReturn(expenses);

        ExpensesSummary expensesSummary = budgetService.calculateExpensesSummary(user, budget.getId());

        assertNotNull(expensesSummary, "Expenses summary should not be null");
        assertEquals(2, expensesSummary.getExpensesSummary().size(), "Expected two categories in the summary");

        BigDecimal totalExpensesCategory1 = new BigDecimal("300");
        assertEquals(totalExpensesCategory1, expensesSummary.getExpensesSummary().get(0).getTotalExpenses(), "Total expenses for Category 1 should be 300");
        assertEquals(BigDecimal.valueOf(42.86), expensesSummary.getExpensesSummary().get(0).getPercentageOfTotal(), "Percentage of total for Category 1 does not match");

        BigDecimal totalExpensesCategory2 = new BigDecimal("400");
        assertEquals(totalExpensesCategory2, expensesSummary.getExpensesSummary().get(1).getTotalExpenses(), "Total expenses for Category 2 should be 400");
        assertEquals(BigDecimal.valueOf(57.14), expensesSummary.getExpensesSummary().get(1).getPercentageOfTotal(), "Percentage of total for Category 2 does not match");

        verify(expenseRepository).findByBudgetIdAndUser(budget.getId(), user);
    }

    @Test
    void saveExpenseFromCsv_ShouldCallRepositorySaveMethod() {
        Expense expense = mock(Expense.class);
        budgetService.saveExpenseFromCsv(expense);
        verify(expenseRepository).save(expense);
    }

    @Test
    void saveIncomeFromCsv_ShouldCallRepositorySaveMethod() {
        Income income = mock(Income.class);
        budgetService.saveIncomeFromCsv(income);
        verify(incomeRepository).save(income);
    }
}