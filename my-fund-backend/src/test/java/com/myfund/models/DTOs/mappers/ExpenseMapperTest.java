package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateExpenseDTO;
import com.myfund.models.DTOs.ExpenseDTO;
import com.myfund.models.Expense;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseMapperTest {

    @Test
    void testCreateExpenseDTOtoExpense() {
        CreateExpenseDTO createExpenseDTO = CreateExpenseDTO.builder()
                .name("Test Expense")
                .amount(new BigDecimal("100.00"))
                .idCategory(1L)
                .idSubCategory(2L)
                .build();

        Expense expense = ExpenseMapper.toModel(createExpenseDTO);

        assertNotNull(expense, "Expense should not be null");
        assertEquals(createExpenseDTO.getName(), expense.getName(), "Expense name should match");
        assertEquals(createExpenseDTO.getAmount(), expense.getAmount(), "Expense amount should match");
        assertEquals(createExpenseDTO.getIdCategory(), expense.getIdCategory(), "Expense category ID should match");
        assertEquals(createExpenseDTO.getIdSubCategory(), expense.getIdSubCategory(), "Expense subcategory ID should match");
    }

    @Test
    void testExpensetoExpenseDTO() {
        Expense expense = Expense.builder()
                .id(1L)
                .name("Test Expense")
                .amount(new BigDecimal("100.00"))
                .idCategory(1L)
                .idSubCategory(2L)
                .localDateTime(LocalDateTime.of(2023, 10, 1, 12, 0))
                .build();

        ExpenseDTO expenseDTO = ExpenseMapper.toDTO(expense);

        assertNotNull(expenseDTO, "ExpenseDTO should not be null");
        assertEquals(expense.getId(), expenseDTO.getId(), "Expense ID should match");
        assertEquals(expense.getName(), expenseDTO.getName(), "Expense name should match");
        assertEquals(expense.getAmount(), expenseDTO.getAmount(), "Expense amount should match");
        assertEquals(expense.getIdCategory(), expenseDTO.getIdCategory(), "Expense category ID should match");
        assertEquals(expense.getIdSubCategory(), expenseDTO.getIdSubCategory(), "Expense subcategory ID should match");
        assertEquals(expense.getLocalDateTime().toLocalDate(), expenseDTO.getLocalDate(), "Expense local date should match");
    }
}