package com.myfund.services.csv;

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
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class SantanderCsvParserTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private SantanderCsvParser santanderCsvParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDelimiter() {
        String delimiter = santanderCsvParser.getDelimiter();
        assertEquals(";", delimiter, "Delimiter should be ';'");
    }

    @Test
    void testIsIncome() {
        String[] values = {"", "", "", "", "", "", "", "", "", "", "1000.00", ""};
        assertTrue(santanderCsvParser.isIncome(values), "Should be identified as income");
    }

    @Test
    void testIsExpense() {
        String[] values = {"", "", "", "", "", "", "", "", "", "", "", "500.00"};
        assertTrue(santanderCsvParser.isExpense(values), "Should be identified as expense");
    }

    @Test
    void testMapToIncome() {
        String[] values = {"", "", "01-01-2023", "", "Salary", "", "", "", "", "", "1000.00", ""};
        Income income = santanderCsvParser.mapToIncome(values);
        assertNotNull(income, "Income should not be null");
        assertEquals(LocalDate.parse("01-01-2023", DateTimeFormatter.ofPattern("dd-MM-yyyy")).atStartOfDay(), income.getLocalDateTime(), "Date should match");
        assertEquals("Salary", income.getName(), "Name should match");
        assertEquals(new BigDecimal("1000.00"), income.getAmount(), "Amount should match");
    }

    @Test
    void testMapToExpense() {
        String[] values = {"", "", "01-01-2023", "", "Groceries", "", "", "", "", "", "", "500.00"};
        Expense expense = santanderCsvParser.mapToExpense(values);
        assertNotNull(expense, "Expense should not be null");
        assertEquals(LocalDate.parse("01-01-2023", DateTimeFormatter.ofPattern("dd-MM-yyyy")).atStartOfDay(), expense.getLocalDateTime(), "Date should match");
        assertEquals("Groceries", expense.getName(), "Name should match");
        assertEquals(new BigDecimal("500.00"), expense.getAmount(), "Amount should match");
    }
}