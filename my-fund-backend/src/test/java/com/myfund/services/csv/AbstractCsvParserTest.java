package com.myfund.services.csv;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.Budget;
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
                BigDecimal amount = new BigDecimal(values[1].replace(',', '.'));
                Income income = Income.builder().amount(amount).build();
                return income;
            }

        @Override
        protected Expense mapToExpense (String[]values){
            BigDecimal amount = new BigDecimal(values[1].replace(',', '.'));
            Expense expense = Expense.builder()
                    .amount(amount)
                    .build();
            return expense;
        }
    }

    ;
}

@Test
void parse_ShouldProcessIncome() throws IOException {

    User user = User.builder().build();
    Long budgetId = 1L;
    Budget budget = Budget.builder()
            .id(budgetId)
            .build();
    when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budget);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("header\nincome,1000".getBytes(StandardCharsets.UTF_8)));

    parser.parse(file, user, budgetId);

    ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
    verify(budgetService, times(1)).saveIncomeFromCsv(incomeCaptor.capture());
    Income savedIncome = incomeCaptor.getValue();
    assertEquals(new BigDecimal("1000"), savedIncome.getAmount());
    assertEquals(user, savedIncome.getUser());
    assertEquals(budget.getId(), savedIncome.getBudget().getId());
}

@Test
void parse_ShouldProcessExpense() throws IOException {

    User user = User.builder().build();
    Long budgetId = 1L;
    Budget budget = Budget.builder()
            .id(budgetId)
            .build();
    when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budget);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("header\nexpense,500".getBytes(StandardCharsets.UTF_8)));

    parser.parse(file, user, budgetId);

    ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
    verify(budgetService, times(1)).saveExpenseFromCsv(expenseCaptor.capture());
    Expense savedExpense = expenseCaptor.getValue();
    assertEquals(new BigDecimal("500"), savedExpense.getAmount());
    assertEquals(user, savedExpense.getUser());
    assertEquals(budget.getId(), savedExpense.getBudget().getId());
}

@Test
void parse_ShouldHandleEmptyFile() throws IOException {

    User user = User.builder().build();
    Long budgetId = 1L;
    Budget budget = Budget.builder()
            .id(budgetId)
            .build();
    when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budget);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("header\n".getBytes(StandardCharsets.UTF_8)));

    parser.parse(file, user, budgetId);

    verify(budgetService, never()).saveIncomeFromCsv(any(Income.class));
    verify(budgetService, never()).saveExpenseFromCsv(any(Expense.class));
}

@Test
void parse_ShouldLogErrorOnException() throws IOException {

    User user = User.builder().build();
    Long budgetId = 1L;
    Budget budget = Budget.builder()
            .id(budgetId)
            .build();
    when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budget);
    when(file.getInputStream()).thenThrow(new IOException("Test Exception"));

    parser.parse(file, user, budgetId);

    verify(budgetService, never()).saveIncomeFromCsv(any(Income.class));
    verify(budgetService, never()).saveExpenseFromCsv(any(Expense.class));
}

@Test
void parse_ShouldSkipInvalidLines() throws IOException {

    User user = User.builder().build();
    Long budgetId = 1L;
    Budget budget = Budget.builder()
            .id(budgetId)
            .build();
    when(budgetService.findBudgetByIdAndUser(budgetId, user)).thenReturn(budget);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("header\ninvalid,line\nincome,1000".getBytes(StandardCharsets.UTF_8)));

    parser.parse(file, user, budgetId);

    ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
    verify(budgetService, times(1)).saveIncomeFromCsv(incomeCaptor.capture());
    Income savedIncome = incomeCaptor.getValue();
    assertEquals(new BigDecimal("1000"), savedIncome.getAmount());
    assertEquals(user, savedIncome.getUser());
    assertEquals(budget.getId(), savedIncome.getBudget().getId());
}

@Test
void processLine_ShouldProcessIncome() throws InvalidInputException {

    User user = User.builder().build();
    Budget budget = Budget.builder()
            .build();
    String line = "income,1000";

    parser.processLine(line, user, budget);

    ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
    verify(budgetService, times(1)).saveIncomeFromCsv(incomeCaptor.capture());
    Income savedIncome = incomeCaptor.getValue();
    assertEquals(new BigDecimal("1000"), savedIncome.getAmount());
    assertEquals(user, savedIncome.getUser());
    assertEquals(budget.getId(), savedIncome.getBudget().getId());
}

@Test
void processLine_ShouldProcessExpense() throws InvalidInputException {

    User user = User.builder().build();
    Budget budget = Budget.builder()
            .build();
    String line = "expense,500";

    parser.processLine(line, user, budget);

    ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
    verify(budgetService, times(1)).saveExpenseFromCsv(expenseCaptor.capture());
    Expense savedExpense = expenseCaptor.getValue();
    assertEquals(new BigDecimal("500"), savedExpense.getAmount());
    assertEquals(user, savedExpense.getUser());
    assertEquals(budget.getId(), savedExpense.getBudget().getId());
}

@Test
void processLine_ShouldNotProcessInvalidLine() throws InvalidInputException {

    User user = User.builder().build();
    Budget budget = Budget.builder()
            .build();
    String line = "invalid,line";

    parser.processLine(line, user, budget);

    verify(budgetService, never()).saveIncomeFromCsv(any(Income.class));
    verify(budgetService, never()).saveExpenseFromCsv(any(Expense.class));
}

@Test
void processLine_ShouldHandleEmptyLine() throws InvalidInputException {

    User user = User.builder().build();
    Budget budget = Budget.builder()
            .build();
    String line = "";

    parser.processLine(line, user, budget);

    verify(budgetService, never()).saveIncomeFromCsv(any(Income.class));
    verify(budgetService, never()).saveExpenseFromCsv(any(Expense.class));
}

@Test
void processIncome_ShouldMapValuesCorrectly() throws InvalidInputException {

    String[] values = {"income", "1000"};
    User user = User.builder().build();

    Budget budget = Budget.builder()
            .build();

    parser.processIncome(values, user, budget);

    ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
    verify(budgetService, times(1)).saveIncomeFromCsv(incomeCaptor.capture());
    Income savedIncome = incomeCaptor.getValue();
    assertEquals(new BigDecimal("1000"), savedIncome.getAmount());
    assertEquals(user, savedIncome.getUser());
    assertEquals(budget.getId(), savedIncome.getBudget().getId());
}

@Test
void processIncome_ShouldCallSaveIncomeFromCsv() throws InvalidInputException {

    String[] values = {"income", "2000"};
    User user = User.builder().build();
    Budget budget = Budget.builder()
            .build();

    parser.processIncome(values, user, budget);

    verify(budgetService, times(1)).saveIncomeFromCsv(any(Income.class));
}

@Test
void processIncome_ShouldSetUserCorrectly() throws InvalidInputException {

    String[] values = {"income", "4000"};
    User user = User.builder().build();
    Budget budget = Budget.builder()
            .build();

    parser.processIncome(values, user, budget);

    ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
    verify(budgetService).saveIncomeFromCsv(incomeCaptor.capture());
    Income savedIncome = incomeCaptor.getValue();
    assertEquals(user, savedIncome.getUser());
}

@Test
void processIncome_ShouldSetBudgetCorrectly() throws InvalidInputException {

    String[] values = {"income", "5000"};
    User user = User.builder().build();
    Budget budget = Budget.builder()
            .build();

    parser.processIncome(values, user, budget);

    ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
    verify(budgetService).saveIncomeFromCsv(incomeCaptor.capture());
    Income savedIncome = incomeCaptor.getValue();
    assertEquals(budget.getId(), savedIncome.getBudget().getId());
}

@Test
void processExpense_ShouldMapValuesToExpense() throws InvalidInputException {

    String[] values = {"Expense", "100.00"};
    User user = User.builder().build();
    Budget budget = Budget.builder()
            .build();

    parser.processExpense(values, user, budget);

    ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
    verify(budgetService).saveExpenseFromCsv(expenseCaptor.capture());
    Expense capturedExpense = expenseCaptor.getValue();

    assertEquals(new BigDecimal("100.00"), capturedExpense.getAmount());
    assertEquals(user, capturedExpense.getUser());
    assertNotNull(capturedExpense.getBudget());
}

@Test
void processExpense_ShouldSetUser() throws InvalidInputException {

    String[] values = {"Expense", "100.00"};
    User user = User.builder().build();

    Budget budget = Budget.builder()
            .build();

    parser.processExpense(values, user, budget);

    ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
    verify(budgetService).saveExpenseFromCsv(expenseCaptor.capture());
    Expense capturedExpense = expenseCaptor.getValue();

    assertEquals(user, capturedExpense.getUser());
}

@Test
void processExpense_ShouldSetBudget() throws InvalidInputException {

    String[] values = {"Expense", "100.00"};
    User user = User.builder().build();

    Budget budget = Budget.builder()
            .build();

    parser.processExpense(values, user, budget);

    ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
    verify(budgetService).saveExpenseFromCsv(expenseCaptor.capture());
    Expense capturedExpense = expenseCaptor.getValue();

    assertNotNull(capturedExpense.getBudget());
}

@Test
void processExpense_ShouldCallSaveExpenseFromCsv() throws InvalidInputException {
    String[] values = {"Expense", "100.00"};
    User user = User.builder().build();

    Budget budget = Budget.builder()
            .build();

    parser.processExpense(values, user, budget);

    verify(budgetService, times(1)).saveExpenseFromCsv(any(Expense.class));
}
}