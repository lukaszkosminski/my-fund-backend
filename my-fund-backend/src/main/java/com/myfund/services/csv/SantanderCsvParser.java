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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Slf4j
public class SantanderCsvParser extends AbstractCsvParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired
    public SantanderCsvParser(BudgetService budgetService) {
        super(budgetService);
    }

    @Override
    protected String getDelimiter() {
        return ";";
    }

    @Override
    protected boolean isIncome(String[] values) {
        return !values[10].isBlank() && values[11].isBlank();
    }

    @Override
    protected boolean isExpense(String[] values) {
        return !values[11].isBlank() && values[10].isBlank();
    }

    @Override
    protected Income mapToIncome(String[] values) {
        String dateColumn = values[2];

        String incomeColumn = values[10];
        String transactionNameColumn = values[4];
        Income income = new Income();
        income.setLocalDateTime(LocalDate.parse(dateColumn, DATE_FORMATTER).atStartOfDay());
        income.setName(transactionNameColumn);
        income.setAmount(new BigDecimal(incomeColumn.replace(',', '.')));

        return income;
    }

    @Override
    protected Expense mapToExpense(String[] values) {
        String dateColumn = values[2];
        String expenseColumn = values[11];
        String transactionNameColumn = values[4];
        Expense expense = new Expense();
        expense.setLocalDateTime(LocalDate.parse(dateColumn, DATE_FORMATTER).atStartOfDay());
        expense.setName(transactionNameColumn);
        expense.setAmount(new BigDecimal(expenseColumn.replace(',', '.')));

        return expense;
    }
}

