package com.myfund.services.csv;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.Budget;
import com.myfund.models.DTOs.BudgetDTO;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.models.User;
import com.myfund.services.BudgetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import com.myfund.models.Expense;
import com.myfund.models.Income;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;


@Slf4j
public abstract class AbstractCsvParser {

    protected final BudgetService budgetService;

    public AbstractCsvParser(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    public void parse(MultipartFile file, User user, Long budgetId) {
        Budget budgetByIdAndUser = budgetService.findBudgetByIdAndUser(budgetId, user);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();
            reader.lines().forEach(line -> processLine(line, user, budgetByIdAndUser));
        } catch (Exception e) {
            log.error("Error processing CSV file", e);
        }
    }

    protected void processLine(String line, User user, Budget budgetByIdAndUser) {
        String[] values = line.split(getDelimiter());
        if (isIncome(values)) {
            processIncome(values, user, budgetByIdAndUser);
        } else if (isExpense(values)) {
            processExpense(values, user, budgetByIdAndUser);
        }
    }

    protected void processIncome(String[] values, User user, Budget budgetByIdAndUser) {
        Income income = mapToIncome(values);
        income.setUser(user);
        income.setBudget(budgetByIdAndUser);
        budgetService.saveIncomeFromCsv(income);
        log.info("Income saved: {}", income);
    }

    protected void processExpense(String[] values, User user, Budget budgetByIdAndUser) {
        Expense expense = mapToExpense(values);
        expense.setUser(user);
        expense.setBudget(budgetByIdAndUser);
        budgetService.saveExpenseFromCsv(expense);
        log.info("Expense saved: {}", expense);
    }

    protected abstract String getDelimiter();

    protected abstract boolean isIncome(String[] values);

    protected abstract boolean isExpense(String[] values);

    protected abstract Income mapToIncome(String[] values);

    protected abstract Expense mapToExpense(String[] values);
}