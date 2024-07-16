package com.myfund.services.csv;

import com.myfund.models.DTOs.BudgetDTO;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.models.Expense;
import com.myfund.models.Income;
import com.myfund.models.User;
import com.myfund.services.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractCsvParserTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private MultipartFile file;

    private AbstractCsvParser parser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new AbstractCsvParser(budgetService) {
            @Override
            protected String getDelimiter() {
                return ",";
            }

            @Override
            protected boolean isIncome(String[] values) {
                return "income".equalsIgnoreCase(values[0]);
            }

            @Override
            protected boolean isExpense(String[] values) {
                return "expense".equalsIgnoreCase(values[0]);
            }

            @Override
            protected Income mapToIncome(String[] values) {
                Income income = new Income();
                income.setAmount(new BigDecimal(values[1].replace(',', '.')));
                return income;
            }

            @Override
            protected Expense mapToExpense(String[] values) {
                Expense expense = new Expense();
                expense.setAmount(new BigDecimal(values[1].replace(',', '.')));
                return expense;
            }
        };
    }

    @Test
    void parse_ShouldProcessIncome() throws IOException {

        User user = new User();
        Long budgetId = 1L;
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(budgetId);
        when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budgetDTO);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("header\nincome,1000".getBytes(StandardCharsets.UTF_8)));

        parser.parse(file, user, budgetId);

        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(budgetService, times(1)).saveIncomeFromCsv(incomeCaptor.capture());
        Income savedIncome = incomeCaptor.getValue();
        assertEquals(new BigDecimal("1000"), savedIncome.getAmount());
        assertEquals(user, savedIncome.getUser());
        assertEquals(budgetDTO.getId(), savedIncome.getBudget().getId());
    }

    @Test
    void parse_ShouldProcessExpense() throws IOException {

        User user = new User();
        Long budgetId = 1L;
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(budgetId);
        when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budgetDTO);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("header\nexpense,500".getBytes(StandardCharsets.UTF_8)));

        parser.parse(file, user, budgetId);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(budgetService, times(1)).saveExpenseFromCsv(expenseCaptor.capture());
        Expense savedExpense = expenseCaptor.getValue();
        assertEquals(new BigDecimal("500"), savedExpense.getAmount());
        assertEquals(user, savedExpense.getUser());
        assertEquals(budgetDTO.getId(), savedExpense.getBudget().getId());
    }

    @Test
    void parse_ShouldHandleEmptyFile() throws IOException {

        User user = new User();
        Long budgetId = 1L;
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(budgetId);
        when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budgetDTO);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("header\n".getBytes(StandardCharsets.UTF_8)));

        parser.parse(file, user, budgetId);

        verify(budgetService, never()).saveIncomeFromCsv(any(Income.class));
        verify(budgetService, never()).saveExpenseFromCsv(any(Expense.class));
    }

    @Test
    void parse_ShouldLogErrorOnException() throws IOException {

        User user = new User();
        Long budgetId = 1L;
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(budgetId);
        when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budgetDTO);
        when(file.getInputStream()).thenThrow(new IOException("Test Exception"));

        parser.parse(file, user, budgetId);

        verify(budgetService, never()).saveIncomeFromCsv(any(Income.class));
        verify(budgetService, never()).saveExpenseFromCsv(any(Expense.class));
    }

    @Test
    void parse_ShouldSkipInvalidLines() throws IOException {

        User user = new User();
        Long budgetId = 1L;
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(budgetId);
        when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budgetDTO);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("header\ninvalid,line\nincome,1000".getBytes(StandardCharsets.UTF_8)));

        parser.parse(file, user, budgetId);

        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(budgetService, times(1)).saveIncomeFromCsv(incomeCaptor.capture());
        Income savedIncome = incomeCaptor.getValue();
        assertEquals(new BigDecimal("1000"), savedIncome.getAmount());
        assertEquals(user, savedIncome.getUser());
        assertEquals(budgetDTO.getId(), savedIncome.getBudget().getId());
    }

    @Test
    void processLine_ShouldProcessIncome() {

        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();
        String line = "income,1000";

        parser.processLine(line, user, budgetDTO);

        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(budgetService, times(1)).saveIncomeFromCsv(incomeCaptor.capture());
        Income savedIncome = incomeCaptor.getValue();
        assertEquals(new BigDecimal("1000"), savedIncome.getAmount());
        assertEquals(user, savedIncome.getUser());
        assertEquals(budgetDTO.getId(), savedIncome.getBudget().getId());
    }

    @Test
    void processLine_ShouldProcessExpense() {

        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();
        String line = "expense,500";

        parser.processLine(line, user, budgetDTO);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(budgetService, times(1)).saveExpenseFromCsv(expenseCaptor.capture());
        Expense savedExpense = expenseCaptor.getValue();
        assertEquals(new BigDecimal("500"), savedExpense.getAmount());
        assertEquals(user, savedExpense.getUser());
        assertEquals(budgetDTO.getId(), savedExpense.getBudget().getId());
    }

    @Test
    void processLine_ShouldNotProcessInvalidLine() {

        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();
        String line = "invalid,line";

        parser.processLine(line, user, budgetDTO);

        verify(budgetService, never()).saveIncomeFromCsv(any(Income.class));
        verify(budgetService, never()).saveExpenseFromCsv(any(Expense.class));
    }

    @Test
    void processLine_ShouldHandleEmptyLine() {

        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();
        String line = "";

        parser.processLine(line, user, budgetDTO);

        verify(budgetService, never()).saveIncomeFromCsv(any(Income.class));
        verify(budgetService, never()).saveExpenseFromCsv(any(Expense.class));
    }

    @Test
    void processIncome_ShouldMapValuesCorrectly() {

        String[] values = {"income", "1000"};
        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();

        parser.processIncome(values, user, budgetDTO);

        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(budgetService, times(1)).saveIncomeFromCsv(incomeCaptor.capture());
        Income savedIncome = incomeCaptor.getValue();
        assertEquals(new BigDecimal("1000"), savedIncome.getAmount());
        assertEquals(user, savedIncome.getUser());
        assertEquals(budgetDTO.getId(), savedIncome.getBudget().getId());
    }

    @Test
    void processIncome_ShouldCallSaveIncomeFromCsv() {

        String[] values = {"income", "2000"};
        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();

        parser.processIncome(values, user, budgetDTO);

        verify(budgetService, times(1)).saveIncomeFromCsv(any(Income.class));
    }

    @Test
    void processIncome_ShouldSetUserCorrectly() {

        String[] values = {"income", "4000"};
        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();

        parser.processIncome(values, user, budgetDTO);

        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(budgetService).saveIncomeFromCsv(incomeCaptor.capture());
        Income savedIncome = incomeCaptor.getValue();
        assertEquals(user, savedIncome.getUser());
    }

    @Test
    void processIncome_ShouldSetBudgetCorrectly() {

        String[] values = {"income", "5000"};
        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();

        parser.processIncome(values, user, budgetDTO);

        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(budgetService).saveIncomeFromCsv(incomeCaptor.capture());
        Income savedIncome = incomeCaptor.getValue();
        assertEquals(budgetDTO.getId(), savedIncome.getBudget().getId());
    }

    @Test
    void processExpense_ShouldMapValuesToExpense() {

        String[] values = {"Expense", "100.00"};
        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();

        parser.processExpense(values, user, budgetDTO);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(budgetService).saveExpenseFromCsv(expenseCaptor.capture());
        Expense capturedExpense = expenseCaptor.getValue();

        assertEquals(new BigDecimal("100.00"), capturedExpense.getAmount());
        assertEquals(user, capturedExpense.getUser());
        assertNotNull(capturedExpense.getBudget());
    }

    @Test
    void processExpense_ShouldSetUser() {

        String[] values = {"Expense", "100.00"};
        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();

        parser.processExpense(values, user, budgetDTO);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(budgetService).saveExpenseFromCsv(expenseCaptor.capture());
        Expense capturedExpense = expenseCaptor.getValue();

        assertEquals(user, capturedExpense.getUser());
    }

    @Test
    void processExpense_ShouldSetBudget() {

        String[] values = {"Expense", "100.00"};
        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();

        parser.processExpense(values, user, budgetDTO);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(budgetService).saveExpenseFromCsv(expenseCaptor.capture());
        Expense capturedExpense = expenseCaptor.getValue();

        assertNotNull(capturedExpense.getBudget());
    }

    @Test
    void processExpense_ShouldCallSaveExpenseFromCsv() {
        String[] values = {"Expense", "100.00"};
        User user = new User();
        BudgetDTO budgetDTO = new BudgetDTO();

        parser.processExpense(values, user, budgetDTO);

        verify(budgetService, times(1)).saveExpenseFromCsv(any(Expense.class));
    }
}