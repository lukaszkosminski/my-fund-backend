package com.myfund.services.csv;

import com.myfund.models.DTOs.BudgetDTO;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.models.Expense;
import com.myfund.models.Income;
import com.myfund.models.User;
import com.myfund.services.BudgetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Slf4j
public class SantanderCsvParser implements CsvParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final BudgetService budgetService;

    @Autowired
    public SantanderCsvParser(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Override
    public void parseCsv(MultipartFile file, User user, Long budgetId) {
        BudgetDTO budgetByIdAndUser = budgetService.findBudgetByIdAndUser(budgetId, user);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[11].isEmpty()) {
                    Income income = mapToIncome(values);
                    income.setUser(user);
                    income.setBudget(BudgetMapper.budgetDTOMapToBudget(budgetByIdAndUser));
                    budgetService.saveIncomeFromCsv(income);
                    log.info("Income saved: {}", income);

                } else if (!values[10].isEmpty()) {
                    Expense expense = mapToExpense(values);
                    expense.setUser(user);
                    expense.setBudget(BudgetMapper.budgetDTOMapToBudget(budgetByIdAndUser));
                    budgetService.saveExpenseFromCsv(expense);
                    log.info("Expense saved: {}", expense);
                }
            }
        } catch (Exception e) {
            log.error("Error processing CSV file", e);
        }
    }

    private Income mapToIncome(String[] values) {
        Income income = new Income();
        income.setLocalDate(LocalDate.parse(values[2], DATE_FORMATTER));
        income.setName(values[3]);
        income.setAmount(new BigDecimal(values[11].replace(',', '.')));

        return income;
    }

    private Expense mapToExpense(String[] values) {
        Expense expense = new Expense();
        expense.setLocalDate(LocalDate.parse(values[2], DATE_FORMATTER));
        expense.setName(values[3]);
        expense.setAmount(new BigDecimal(values[10].replace(',', '.')));

        return expense;
    }
}
