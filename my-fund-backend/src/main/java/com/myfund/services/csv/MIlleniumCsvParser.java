package com.myfund.services.csv;

import com.myfund.models.DTOs.BudgetDTO;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.models.Expense;
import com.myfund.models.Income;
import com.myfund.models.User;
import com.myfund.services.BudgetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class MIlleniumCsvParser implements CsvParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BudgetService budgetService;

    public MIlleniumCsvParser(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Override
    public void parse(MultipartFile file, User user, Long budgetId) {
        BudgetDTO budgetByIdAndUser = budgetService.findBudgetByIdAndUser(budgetId, user);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().skip(1).forEach(line -> {
                String[] values = line.split(",");
                String incomeColumn = values[8];
                String expenseColumn = values[7];
                if (expenseColumn.isBlank() && !incomeColumn.isBlank()) {
                    Income income = mapToIncome(values);
                    income.setUser(user);
                    income.setBudget(BudgetMapper.budgetDTOMapToBudget(budgetByIdAndUser));
                    budgetService.saveIncomeFromCsv(income);
                    log.info("Income saved: {}", income);

                } else if (incomeColumn.isBlank() && !expenseColumn.isBlank()) {
                    Expense expense = mapToExpense(values);
                    expense.setUser(user);
                    expense.setBudget(BudgetMapper.budgetDTOMapToBudget(budgetByIdAndUser));
                    budgetService.saveExpenseFromCsv(expense);
                    log.info("Expense saved: {}", expense);
                }
            });
        } catch (Exception e) {
            log.error("Error processing CSV file", e);
        }
    }

    private Income mapToIncome(String[] values) {
        String dateColumn = values[1];
        String incomeColumn = values[8];
        String transactionNameColumn = values[6];
        Income income = new Income();
        income.setLocalDate(LocalDate.parse(dateColumn, DATE_FORMATTER));
        income.setName(transactionNameColumn);
        income.setAmount(stringToBigDecimal(incomeColumn));

        return income;
    }

    private Expense mapToExpense(String[] values) {
        String dateColumn = values[1];
        String expenseColumn = values[7].substring(1);
        String transactionNameColumn = values[6];
        Expense expense = new Expense();
        expense.setLocalDate(LocalDate.parse(dateColumn, DATE_FORMATTER));
        expense.setName(transactionNameColumn);
        expense.setAmount(stringToBigDecimal(expenseColumn));

        return expense;
    }

    private BigDecimal stringToBigDecimal(String str) {
        if (!str.isBlank()) {
            try {
                return new BigDecimal(str.replace(',', '.'));
            } catch (NumberFormatException e) {
                log.error("Invalid number format: {}", str, e);
            }
        } else {
            log.warn("Attempting to convert a null or empty string to BigDecimal");
        }
        return BigDecimal.ZERO;
    }
}
