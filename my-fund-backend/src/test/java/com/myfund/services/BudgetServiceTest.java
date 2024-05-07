package com.myfund.services;

import com.myfund.exceptions.BudgetNotFoundException;
import com.myfund.exceptions.BudgetNotUniqueException;
import com.myfund.exceptions.SubcategoryNotRelatedToCategoryException;
import com.myfund.models.Budget;
import com.myfund.models.DTOs.*;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.models.Expense;
import com.myfund.models.User;
import com.myfund.repositories.BudgetRepository;
import com.myfund.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    void createBudget_NewBudget_Success() {

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
    void createExpenseWhenBudgetExistsAndCategoryIsValid() {
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

}