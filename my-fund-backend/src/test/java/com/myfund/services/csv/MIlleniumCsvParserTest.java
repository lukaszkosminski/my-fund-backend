package com.myfund.services.csv;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.Expense;
import com.myfund.models.Income;
import com.myfund.services.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MIlleniumCsvParserTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private MIlleniumCsvParser parser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDelimiter() {
        assertEquals(",", parser.getDelimiter(), "Delimiter should be a comma");
    }

    @Test
    void testIsIncome() {
        String[] values = {"", "", "", "", "", "", "", "", "100.00"};
        assertTrue(parser.isIncome(values), "Should identify as income");
    }

    @Test
    void testIsExpense() {
        String[] values = {"", "", "", "", "", "", "", "50.00", ""};
        assertTrue(parser.isExpense(values), "Should identify as expense");
    }

    @Test
    void testMapToIncome() throws InvalidInputException {
        String[] values = {"", "2023-10-01", "", "", "", "", "Salary", "", "1000.00"};
        Income income = parser.mapToIncome(values);
        assertNotNull(income, "Income should not be null");
        assertEquals(LocalDate.of(2023, 10, 1).atStartOfDay(), income.getLocalDateTime(), "Income date should match");
        assertEquals("Salary", income.getName(), "Income name should match");
        assertEquals(new BigDecimal("1000.00"), income.getAmount(), "Income amount should match");
    }

    @Test
    void testMapToExpense() throws InvalidInputException {
        String[] values = {"", "2023-10-01", "", "", "", "", "Groceries", "50.00", ""};
        Expense expense = parser.mapToExpense(values);
        assertNotNull(expense, "Expense should not be null");
        assertEquals(LocalDate.of(2023, 10, 1).atStartOfDay(), expense.getLocalDateTime(), "Expense date should match");
        assertEquals("Groceries", expense.getName(), "Expense name should match");
        assertEquals(new BigDecimal("50.00"), expense.getAmount(), "Expense amount should match");
    }
}