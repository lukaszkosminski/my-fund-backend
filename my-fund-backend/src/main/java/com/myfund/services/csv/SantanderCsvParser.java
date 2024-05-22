package com.myfund.services.csv;

import com.myfund.models.Expense;
import com.myfund.models.Income;
import com.myfund.services.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SantanderCsvParser implements CsvParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final BudgetService budgetService;

    @Autowired
    public SantanderCsvParser(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Override
    public void parseCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[11].isEmpty()) {
                    Income income = mapToIncome(values);
                    budgetService.saveIncomeFromCsv(income);

                } else if (!values[10].isEmpty()) {
                    Expense expense = mapToExpense(values);
                    budgetService.saveExpenseFromCsv(expense);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

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
