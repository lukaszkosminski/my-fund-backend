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
        User user = new User();
        user.setEmail("test@example.com");

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

        CreateBudgetDTO createBudgetDTO = new CreateBudgetDTO();
        createBudgetDTO.setName("Test Budget");
        User user = new User();
        user.setEmail("test@example.com");

        when(budgetRepository.findByNameAndUser(anyString(), any(User.class))).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetMapper.createBudgetDTOMapToBudget(createBudgetDTO);
        BudgetDTO result = budgetService.createBudget(createBudgetDTO, user);

        assertNotNull(result);
        assertEquals("Test Budget", result.getName());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void createBudget_DuplicateBudget_ThrowsException() {

        CreateBudgetDTO createBudgetDTO = new CreateBudgetDTO();
        createBudgetDTO.setName("Existing Budget");
        User user = new User();
        user.setEmail("user@example.com");

        Budget existingBudget = new Budget();
        existingBudget.setName("Existing Budget");
        existingBudget.setUser(user);
        existingBudget.setLocalDateTime(LocalDateTime.now());
        existingBudget.setBalance(BigDecimal.ZERO);
        existingBudget.setTotalExpense(BigDecimal.ZERO);
        existingBudget.setTotalIncome(BigDecimal.ZERO);

        when(budgetRepository.findByNameAndUser(anyString(), any(User.class))).thenReturn(Optional.of(existingBudget));

        assertThrows(BudgetNotUniqueException.class, () -> budgetService.createBudget(createBudgetDTO, user));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void findAllBudgetsByUser_ReturnsBudgetSummaryList() {

        User user = new User();
        user.setId(1L);

        Budget budget1 = new Budget();
        Budget budget2 = new Budget();
        List<Budget> budgets = Arrays.asList(budget1, budget2);

        when(budgetRepository.findAllByUser(user)).thenReturn(budgets);

        List<BudgetSummaryDTO> result = budgetService.findAllBudgetsByUser(user);

        assertNotNull(result, "The result should not be null");
        assertEquals(2, result.size(), "The result list should contain two budget summaries");
        verify(budgetRepository).findAllByUser(user);
        verifyNoMoreInteractions(budgetRepository);
    }

    @Test
    void findBudgetByIdAndUser_WhenBudgetExists() {

        Budget budget = new Budget();
        User user = new User();
        when(budgetRepository.findByIdAndUser(any(Long.class), any(User.class))).thenReturn(Optional.of(budget));

        BudgetDTO result = budgetService.findBudgetByIdAndUser(1L, user);

        assertNotNull(result);

        verify(budgetRepository).findByIdAndUser(1L, user);
    }

    @Test
    void findBudgetByIdAndUser_WhenBudgetDoesNotExist() {

        User user = new User();
        when(budgetRepository.findByIdAndUser(any(Long.class), any(User.class))).thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class, () -> budgetService.findBudgetByIdAndUser(1L, user));

        verify(budgetRepository).findByIdAndUser(1L, user);
    }

    @Test
    void createExpenseWhenBudgetExistsAndCategoryIsValid() throws InvalidInputException {

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setTotalIncome(new BigDecimal("100"));
        User user = new User();
        CreateExpenseDTO createExpenseDTO = new CreateExpenseDTO();
        createExpenseDTO.setIdCategory(1L);
        createExpenseDTO.setIdSubCategory(2L);
        createExpenseDTO.setAmount(new BigDecimal("100"));
        createExpenseDTO.setName("Test Expense");

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(budget));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(true);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = budgetService.createExpense(1L, createExpenseDTO, user);

        assertNotNull(result);
        verify(expenseRepository).save(any(Expense.class));
        verify(budgetRepository).findByIdAndUser(anyLong(), any(User.class));
    }

    @Test
    void createExpenseWhenBudgetExistsButCategoryIsInvalid() {

        Budget budget = new Budget();
        CreateExpenseDTO createExpenseDTO = new CreateExpenseDTO();
        createExpenseDTO.setIdCategory(1L);
        createExpenseDTO.setIdSubCategory(2L);
        User user = new User();

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(budget));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(false);

        assertThrows(SubcategoryNotRelatedToCategoryException.class, () -> budgetService.createExpense(1L, createExpenseDTO, user));

        verify(budgetRepository).findByIdAndUser(anyLong(), any(User.class));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpenseWhenBudgetDoesNotExist() {

        CreateExpenseDTO createExpenseDTO = new CreateExpenseDTO();
        User user = new User();
        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class, () -> budgetService.createExpense(1L, createExpenseDTO, user));

        verify(budgetRepository).findByIdAndUser(anyLong(), any(User.class));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createIncome_WhenBudgetExistsAndCategoryIsValid() throws InvalidInputException {

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setTotalIncome(new BigDecimal("100"));
        budget.setTotalExpense(new BigDecimal("100"));
        User user = new User();
        CreateIncomeDTO createIncomeDTO = new CreateIncomeDTO();
        createIncomeDTO.setIdCategory(1L);
        createIncomeDTO.setIdSubCategory(2L);
        createIncomeDTO.setAmount(new BigDecimal("100"));
        createIncomeDTO.setName("Test Income");

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(budget));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(true);
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));
        IncomeDTO result = budgetService.createIncome(1L, createIncomeDTO, user);

        assertNotNull(result);
        verify(incomeRepository).save(any(Income.class));
        verify(budgetRepository).findByIdAndUser(anyLong(), any(User.class));
    }

    @Test
    void createIncome_WhenBudgetDoesNotExist() {
        CreateIncomeDTO createIncomeDTO = new CreateIncomeDTO();
        User user = new User();

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class, () -> budgetService.createIncome(1L, createIncomeDTO, user));

        verify(budgetRepository).findByIdAndUser(anyLong(), any(User.class));
        verify(incomeRepository, never()).save(any(Income.class));
    }

    @Test
    void createIncome_WhenBudgetExistsButCategoryIsInvalid() {
        Budget budget = new Budget();
        CreateIncomeDTO createIncomeDTO = new CreateIncomeDTO();
        createIncomeDTO.setIdCategory(1L);
        createIncomeDTO.setIdSubCategory(2L);
        User user = new User();

        when(budgetRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.of(budget));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(false);

        assertThrows(SubcategoryNotRelatedToCategoryException.class, () -> budgetService.createIncome(1L, createIncomeDTO, user));

        verify(budgetRepository).findByIdAndUser(anyLong(), any(User.class));
        verify(incomeRepository, never()).save(any(Income.class));
    }

    @Test
    void updateExpense_ExpenseFoundAndUpdated() {

        User user = new User();
        user.setId(1L);

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setUser(user);

        CreateExpenseDTO createExpenseDTO = new CreateExpenseDTO();
        createExpenseDTO.setIdCategory(1L);
        createExpenseDTO.setIdSubCategory(2L);
        createExpenseDTO.setAmount(new BigDecimal("200"));
        createExpenseDTO.setName("Test Expense");

        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(true);
        when(expenseRepository.findByIdAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = budgetService.updateExpense(1L, 1L, createExpenseDTO, user);

        assertNotNull(result);
        assertEquals(createExpenseDTO.getAmount(), result.getAmount());
        assertEquals(createExpenseDTO.getName(), result.getName());
    }

    @Test
    void updateExpense_ExpenseNotFound() {

        User user = new User();
        user.setId(1L);

        CreateExpenseDTO createExpenseDTO = new CreateExpenseDTO();
        createExpenseDTO.setIdCategory(1L);
        createExpenseDTO.setIdSubCategory(2L);
        createExpenseDTO.setAmount(new BigDecimal("200"));
        createExpenseDTO.setName("Test Expense");

        when(expenseRepository.findByIdAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () -> budgetService.updateExpense(1L, 1L, createExpenseDTO, user));
    }

    @Test
    void updateExpense_SubcategoryNotRelated() {

        User user = new User();
        user.setId(1L);

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setUser(user);

        CreateExpenseDTO createExpenseDTO = new CreateExpenseDTO();
        createExpenseDTO.setIdCategory(1L);
        createExpenseDTO.setIdSubCategory(2L);
        createExpenseDTO.setAmount(new BigDecimal("200"));
        createExpenseDTO.setName("Test Expense");

        when(expenseRepository.findByIdAndUserIdAndBudgetId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(expense));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenThrow(new SubcategoryNotRelatedToCategoryException("Subcategory not related"));

        assertThrows(SubcategoryNotRelatedToCategoryException.class, () -> budgetService.updateExpense(1L, 1L, createExpenseDTO, user));
    }

    @Test
    void updateIncome_Success() {

        User user = new User();
        user.setId(1L);
        CreateIncomeDTO createIncomeDTO = new CreateIncomeDTO();
        createIncomeDTO.setIdCategory(1L);
        createIncomeDTO.setIdSubCategory(1L);
        createIncomeDTO.setAmount(new BigDecimal("200"));
        createIncomeDTO.setName("Salary");
        Income income = new Income();
        income.setId(1L);
        income.setUser(user);
        income.setAmount(createIncomeDTO.getAmount());
        income.setName(createIncomeDTO.getName());

        Long budgetId = 1L;
        Long incomeId = 1L;

        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.of(income));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenReturn(true);
        when(incomeRepository.save(any(Income.class))).thenReturn(income);

        IncomeDTO result = budgetService.updateIncome(budgetId, incomeId, createIncomeDTO, user);

        assertNotNull(result);
        assertEquals(createIncomeDTO.getName(), result.getName());
        verify(incomeRepository).save(any(Income.class));
    }

    @Test
    void updateIncome_IncomeNotFound() {
        User user = new User();
        user.setId(1L);
        CreateIncomeDTO createIncomeDTO = new CreateIncomeDTO();
        Long budgetId = 1L;
        Long incomeId = 1L;

        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.empty());

        assertThrows(IncomeNotFoundException.class, () -> budgetService.updateIncome(budgetId, incomeId, createIncomeDTO, user));
    }

    @Test
    void updateIncome_SubcategoryNotRelatedToCategory() {

        User user = new User();
        user.setId(1L);
        CreateIncomeDTO createIncomeDTO = new CreateIncomeDTO();
        createIncomeDTO.setIdCategory(1L);
        createIncomeDTO.setIdSubCategory(1L);
        Income income = new Income();
        income.setId(1L);
        Long budgetId = 1L;
        Long incomeId = 1L;

        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.of(income));
        when(categoryService.isSubcategoryRelatedToCategory(anyLong(), anyLong(), any(User.class))).thenThrow(new SubcategoryNotRelatedToCategoryException("Subcategory not related"));

        assertThrows(SubcategoryNotRelatedToCategoryException.class, () -> budgetService.updateIncome(budgetId, incomeId, createIncomeDTO, user));
    }

    @Test
    void validateCategoryAndSubCategory_BothNull() {
        User user = new User();
        user.setId(1L);
        assertTrue(budgetService.validateCategoryAndSubCategory(null, null, user));
    }

    @Test
    void validateCategoryAndSubCategory_CategoryNotNullSubCategoryNull() {
        User user = new User();
        user.setId(1L);
        assertTrue(budgetService.validateCategoryAndSubCategory(1L, null, user));
    }

    @Test
    void validateCategoryAndSubCategory_CategoryNullSubCategoryNotNull_ThrowsException() {
        User user = new User();
        user.setId(1L);
        assertThrows(SubcategoryNotFoundException.class, () -> budgetService.validateCategoryAndSubCategory(null, 1L, user));
    }

    @Test
    void validateCategoryAndSubCategory_BothNotNullAndRelated() {
        User user = new User();
        user.setId(1L);
        when(categoryService.isSubcategoryRelatedToCategory(1L, 1L, user)).thenReturn(true);
        assertTrue(budgetService.validateCategoryAndSubCategory(1L, 1L, user));
        verify(categoryService).isSubcategoryRelatedToCategory(1L, 1L, user);
    }

    @Test
    void validateCategoryAndSubCategory_BothNotNullAndNotRelated() {
        User user = new User();
        user.setId(1L);
        when(categoryService.isSubcategoryRelatedToCategory(2L, 1L, user)).thenReturn(false);
        assertFalse(budgetService.validateCategoryAndSubCategory(1L, 2L, user));
        verify(categoryService).isSubcategoryRelatedToCategory(2L, 1L, user);
    }

    @Test
    void updateExpensesCategoryIdToNull() {

        Long categoryId = 1L;
        List<Expense> expenses = Arrays.asList(new Expense(), new Expense());
        when(expenseRepository.findByIdCategory(categoryId)).thenReturn(expenses);

        budgetService.updateExpensesCategoryIdToNull(categoryId);

        verify(expenseRepository, times(1)).findByIdCategory(categoryId);
        for (Expense expense : expenses) {
            assertNull(expense.getIdCategory(), "Category ID should be null");
            assertNull(expense.getIdSubCategory(), "Subcategory ID should be null");
        }
        verify(expenseRepository, times(expenses.size())).save(any(Expense.class));
    }

    @Test
    void updateIncomesCategoryIdToNull() {

        Long idCategory = 1L;
        List<Income> incomes = Arrays.asList(new Income(), new Income());
        when(incomeRepository.findByIdCategory(idCategory)).thenReturn(incomes);

        budgetService.updateIncomesCategoryIdToNull(idCategory);

        verify(incomeRepository, times(1)).findByIdCategory(idCategory);
        for (Income income : incomes) {
            assertNull(income.getIdCategory(), "Category ID should be null");
            assertNull(income.getIdSubCategory(), "Subcategory ID should be null");
        }

        verify(incomeRepository, times(incomes.size())).save(any(Income.class));
    }

    @Test
    void updateExpensesSubcategoryIdToNull() {

        Long subcategoryId = 1L;
        List<Expense> expenses = Arrays.asList(new Expense(), new Expense());
        when(expenseRepository.findByIdSubCategory(subcategoryId)).thenReturn(expenses);

        budgetService.updateExpensesSubcategoryIdToNull(subcategoryId);

        verify(expenseRepository, times(1)).findByIdSubCategory(subcategoryId);
        expenses.forEach(expense -> assertNull(expense.getIdSubCategory()));

        verify(expenseRepository, times(expenses.size())).save(any(Expense.class));
    }

    @Test
    void updateIncomesSubcategoryIdToNull() {

        Long subcategoryId = 1L;
        List<Income> incomes = Arrays.asList(new Income(), new Income());
        incomes.forEach(income -> income.setIdSubCategory(subcategoryId));

        when(incomeRepository.findByIdSubCategory(subcategoryId)).thenReturn(incomes);

        budgetService.updateIncomesSubcategoryIdToNull(subcategoryId);

        verify(incomeRepository, times(1)).findByIdSubCategory(subcategoryId);
        incomes.forEach(income -> assertNull(income.getIdSubCategory()));
        verify(incomeRepository, times(incomes.size())).save(any(Income.class));

    }

    @Test
    void getTotalExpensesByCategory_ReturnsCorrectValue() {

        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);
        BigDecimal expectedTotalExpenses = new BigDecimal("100.00");
        FinancialAggregate financialAggregate = new FinancialAggregate();
        financialAggregate.setValue(expectedTotalExpenses);

        when(expenseRepository.sumExpensesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId())).thenReturn(expectedTotalExpenses);

        FinancialAggregateCategoryDTO result = budgetService.getTotalExpensesByCategory(budgetId, categoryId, user);

        assertNotNull(result);
        verify(expenseRepository, times(1)).sumExpensesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId());
        assertEquals(expectedTotalExpenses, result.getValue());

    }

    @Test
    void getTotalExpensesByCategory_NoExpensesFound() {
        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);
        when(expenseRepository.sumExpensesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId())).thenReturn(null);

        FinancialAggregateCategoryDTO resultDTO = budgetService.getTotalExpensesByCategory(budgetId, categoryId, user);

        assertNotNull(resultDTO);
        assertEquals(BigDecimal.ZERO, resultDTO.getValue());

    }

    @Test
    void getTotalExpensesByCategory_ExceptionThrown() {
        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);
        when(expenseRepository.sumExpensesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId())).thenThrow(new RuntimeException("Database error"));

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
        User user = new User();
        user.setId(1L);
        BigDecimal expectedTotalExpenses = new BigDecimal("100.00");

        when(expenseRepository.sumExpensesByBudgetIdAndSubcategoryIdAndUserId(budgetId, subcategoryId, user.getId())).thenReturn(expectedTotalExpenses);

        FinancialAggregateSubcategoryDTO result = budgetService.getTotalExpensesBySubcategory(budgetId, subcategoryId, user);

        assertNotNull(result);
        assertEquals(expectedTotalExpenses, result.getValue());
    }

    @Test
    void getTotalExpensesBySubcategory_NoExpensesFound() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        when(expenseRepository.sumExpensesByBudgetIdAndSubcategoryIdAndUserId(budgetId, subcategoryId, user.getId())).thenReturn(null);

        FinancialAggregateSubcategoryDTO result = budgetService.getTotalExpensesBySubcategory(budgetId, subcategoryId, user);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getValue());
    }

    @Test
    void getTotalExpensesBySubcategory_ThrowsException() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);

        when(expenseRepository.sumExpensesByBudgetIdAndSubcategoryIdAndUserId(budgetId, subcategoryId, user.getId())).thenThrow(new RuntimeException("Database error"));


        assertThrows(TotalExpensesRetrievalException.class, () -> budgetService.getTotalExpensesBySubcategory(budgetId, subcategoryId, user));
    }

    @Test
    void getTotalIncomesByCategory_WithIncomes() {

        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);
        BigDecimal expectedTotalIncomes = new BigDecimal("1000");
        when(incomeRepository.sumIncomesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId())).thenReturn(expectedTotalIncomes);


        FinancialAggregateCategoryDTO result = budgetService.getTotalIncomesByCategory(budgetId, categoryId, user);


        assertNotNull(result);
        assertEquals(expectedTotalIncomes, result.getValue());
        verify(incomeRepository, times(1)).sumIncomesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId());
    }

    @Test
    void getTotalIncomesByCategory_WithNoIncomes() {

        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);
        when(incomeRepository.sumIncomesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId())).thenReturn(null);


        FinancialAggregateCategoryDTO result = budgetService.getTotalIncomesByCategory(budgetId, categoryId, user);


        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getValue());
        verify(incomeRepository, times(1)).sumIncomesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId());
    }

    @Test
    void getTotalIncomesByCategory_ThrowsException() {

        Long budgetId = 1L;
        Long categoryId = 1L;
        User user = new User();
        user.setId(1L);
        when(incomeRepository.sumIncomesByBudgetIdAndCategoryIdAndUserId(anyLong(), anyLong(), anyLong())).thenThrow(new RuntimeException("Database error"));


        assertThrows(TotalIncomesRetrievalException.class, () -> budgetService.getTotalIncomesByCategory(budgetId, categoryId, user));
        verify(incomeRepository, times(1)).sumIncomesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId());
    }

    @Test
    void getTotalIncomesBySubcategoryReturnsCorrectValue() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);
        BigDecimal expectedTotalIncomes = new BigDecimal("1000");
        when(incomeRepository.sumIncomesByBudgetIdAndSubcategoryIdAndUserId(budgetId, subcategoryId, user.getId())).thenReturn(expectedTotalIncomes);

        FinancialAggregateSubcategoryDTO result = budgetService.getTotalIncomesBySubcategory(budgetId, subcategoryId, user);

        assertNotNull(result);
        assertEquals(expectedTotalIncomes, result.getValue());
    }

    @Test
    void getTotalIncomesBySubcategoryReturnsZeroWhenNoIncomesFound() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);
        when(incomeRepository.sumIncomesByBudgetIdAndSubcategoryIdAndUserId(budgetId, subcategoryId, user.getId())).thenReturn(null);

        FinancialAggregateSubcategoryDTO result = budgetService.getTotalIncomesBySubcategory(budgetId, subcategoryId, user);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getValue());
    }

    @Test
    void getTotalIncomesBySubcategoryThrowsExceptionOnFailure() {

        Long budgetId = 1L;
        Long subcategoryId = 1L;
        User user = new User();
        user.setId(1L);
        when(incomeRepository.sumIncomesByBudgetIdAndSubcategoryIdAndUserId(anyLong(), anyLong(), anyLong())).thenThrow(new RuntimeException("Database error"));

        assertThrows(TotalIncomesRetrievalException.class, () -> budgetService.getTotalIncomesBySubcategory(budgetId, subcategoryId, user));
    }

    @Test
    void deleteBudgetByIdAndUser_WhenBudgetExists() {

        User testUser = new User();
        testUser.setId(1L);
        Long testBudgetId = 2L;
        when(budgetRepository.existsByIdAndUserId(testBudgetId, testUser.getId())).thenReturn(true);

        budgetService.deleteBudgetByIdAndUser(testBudgetId, testUser);

        verify(budgetRepository).deleteBudgetByIdAndUser(testBudgetId, testUser);
        verify(expenseRepository).findByBudgetId(testBudgetId);
        verify(incomeRepository).findByBudgetId(testBudgetId);
    }

    @Test
    void deleteBudgetByIdAndUser_WhenBudgetDoesNotExist() {

        User testUser = new User();
        testUser.setId(1L);
        Long testBudgetId = 2L;
        when(budgetRepository.existsByIdAndUserId(testBudgetId, testUser.getId())).thenReturn(false);

        assertThrows(BudgetNotFoundException.class, () -> budgetService.deleteBudgetByIdAndUser(testBudgetId, testUser));
    }

    @Test
    void deleteExpenseByIdAndUser_ExpenseExists() {

        Long expenseId = 1L;
        Long budgetId = 1L;
        User user = new User();
        user.setId(1L);

        when(expenseRepository.findByIdAndUserIdAndBudgetId(expenseId, user.getId(), budgetId)).thenReturn(Optional.of(new Expense()));

        budgetService.deleteExpenseByIdAndUser(expenseId, user, budgetId);

        verify(expenseRepository, times(1)).deleteExpenseByIdAndUserAndBudgetId(expenseId, user, budgetId);
    }

    @Test
    void deleteExpenseByIdAndUser_ExpenseDoesNotExist_ThrowsException() {

        Long expenseId = 1L;
        Long budgetId = 1L;
        User user = new User();
        user.setId(1L);

        when(expenseRepository.findByIdAndUserIdAndBudgetId(expenseId, user.getId(), budgetId)).thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () -> budgetService.deleteExpenseByIdAndUser(expenseId, user, budgetId));

        verify(expenseRepository, never()).deleteExpenseByIdAndUserAndBudgetId(anyLong(), any(User.class), anyLong());
    }

    @Test
    void deleteIncomeByIdAndUser_WhenIncomeExists() {

        User user = new User();
        user.setId(1L);
        Long incomeId = 2L;
        Long budgetId = 3L;

        Income income = new Income();
        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.of(income));

        budgetService.deleteIncomeByIdAndUser(incomeId, user, budgetId);

        verify(incomeRepository).deleteExpenseByIdAndUserAndBudgetId(incomeId, user, budgetId);
    }

    @Test
    void deleteIncomeByIdAndUser_WhenIncomeDoesNotExist_ThrowsException() {

        User user = new User();
        user.setId(1L);
        Long incomeId = 2L;
        Long budgetId = 3L;

        when(incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId)).thenReturn(Optional.empty());

        assertThrows(IncomeNotFoundException.class, () -> budgetService.deleteIncomeByIdAndUser(incomeId, user, budgetId));

        verify(incomeRepository, never()).deleteExpenseByIdAndUserAndBudgetId(any(), any(), any());
    }


    @Test
    public void testNoExpenses() {
        User user = new User();
        user.setId(1L);
        Long budgetId = 1L;

        when(expenseRepository.findByBudgetIdAndUser(budgetId, user)).thenReturn(new ArrayList<>());
        ExpensesSummaryDTO expensesSummary = budgetService.calculateExpensesSummary(user, budgetId);
        assertTrue(expensesSummary.getExpensesSummary().isEmpty());
    }

    @Test
    public void testExpensesInOneCategory() {
        User user = new User();
        user.setId(1L);
        Budget budget = new Budget();
        budget.setId(1L);

        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setAmount(BigDecimal.valueOf(100));
        expense1.setBudget(budget);
        expense1.setIdCategory(1L);
        expense1.setIdSubCategory(1L);
        expense1.setUser(user);

        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setAmount(BigDecimal.valueOf(200));
        expense2.setBudget(budget);
        expense2.setIdCategory(1L);
        expense2.setIdSubCategory(1L);
        expense2.setUser(user);

        List<Expense> expenses = List.of(expense1, expense2);

        when(expenseRepository.findByBudgetIdAndUser(budget.getId(), user)).thenReturn(expenses);

        ExpensesSummaryDTO expensesSummary = budgetService.calculateExpensesSummary(user, budget.getId());
        assertEquals(1, expensesSummary.getExpensesSummary().size());
        assertEquals(BigDecimal.valueOf(300), expensesSummary.getExpensesSummary().get(0).getTotalExpenses());
        assertEquals(BigDecimal.valueOf(100), expensesSummary.getExpensesSummary().get(0).getSubcategories().get(0).getExpenseAmount());
        assertEquals(new BigDecimal("100.00"), expensesSummary.getExpensesSummary().get(0).getPercentageOfTotal());
    }

    @Test
    public void testExpensesInMultipleCategoriesWithPercentageOfTotal() {
        User user = new User();
        user.setId(1L);
        Budget budget = new Budget();
        budget.setId(1L);

        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setAmount(new BigDecimal("100"));
        expense1.setBudget(budget);
        expense1.setIdCategory(1L);
        expense1.setIdSubCategory(1L);
        expense1.setUser(user);

        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setAmount(new BigDecimal("200"));
        expense2.setBudget(budget);
        expense2.setIdCategory(1L);
        expense2.setIdSubCategory(2L);
        expense2.setUser(user);

        Expense expense3 = new Expense();
        expense3.setId(3L);
        expense3.setAmount(new BigDecimal("150"));
        expense3.setBudget(budget);
        expense3.setIdCategory(2L);
        expense3.setIdSubCategory(3L);
        expense3.setUser(user);

        Expense expense4 = new Expense();
        expense4.setId(4L);
        expense4.setAmount(new BigDecimal("250"));
        expense4.setBudget(budget);
        expense4.setIdCategory(2L);
        expense4.setIdSubCategory(4L);
        expense4.setUser(user);

        List<Expense> expenses = List.of(expense1, expense2, expense3, expense4);

        when(expenseRepository.findByBudgetIdAndUser(budget.getId(), user)).thenReturn(expenses);

        ExpensesSummaryDTO expensesSummary = budgetService.calculateExpensesSummary(user, budget.getId());

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