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
        return !values[11].isEmpty() && values[10].isEmpty() && values[9].isEmpty();
    }

    @Override
    protected boolean isExpense(String[] values) {
        return !values[10].isEmpty() && values[11].isEmpty() && values[9].isEmpty();
    }

    @Override
    protected Income mapToIncome(String[] values) {
        System.out.println(values[3]);
        String dateColumn = values[2];
        String incomeColumn = values[11];
        String transactionNameColumn = values[3];
        LocalDateTime dateTime = LocalDate.parse(dateColumn, DATE_FORMATTER).atStartOfDay();
        BigDecimal amount = new BigDecimal(incomeColumn.replace(',', '.'));

        Income income = Income.builder()
                .name(transactionNameColumn)
                .amount(amount)
                .localDateTime(dateTime)
                .build();

        return income;
    }

    @Override
    protected Expense mapToExpense(String[] values) {
        System.out.println(values[3]);
        String dateColumn = values[2];
        String expenseColumn = values[10];
        String transactionNameColumn = values[3];
        BigDecimal amount = new BigDecimal(expenseColumn.replace(',', '.'));
        LocalDateTime dateTime = LocalDate.parse(dateColumn, DATE_FORMATTER).atStartOfDay();

        Expense expense =Expense.builder()
                .name(transactionNameColumn)
                .amount(amount)
                .localDateTime(dateTime)
                .build();

        return expense;
    }
}

