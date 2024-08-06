package com.myfund.services.csv;

import com.myfund.exceptions.InvalidInputException;
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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class MIlleniumCsvParser extends AbstractCsvParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public MIlleniumCsvParser(BudgetService budgetService) {
        super(budgetService);
    }

    @Override
    protected String getDelimiter() {
        return ",";
    }

    @Override
    protected boolean isIncome(String[] values) {
        return values[7].isBlank() && !values[8].isBlank();
    }

    @Override
    protected boolean isExpense(String[] values) {
        return values[8].isBlank() && !values[7].isBlank();
    }

    @Override
    protected Income mapToIncome(String[] values){
        String dateColumn = values[1];
        String incomeColumn = values[8];
        String transactionNameColumn = values[6];

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
        String dateColumn = values[1];
        String expenseColumn = values[7];
        String transactionNameColumn = values[6];
        LocalDateTime dateTime = LocalDate.parse(dateColumn, DATE_FORMATTER).atStartOfDay();
        BigDecimal amount = new BigDecimal(expenseColumn.replace(',', '.'));

        Expense expense = Expense.builder()
                .name(transactionNameColumn)
                .amount(amount)
                .localDateTime(dateTime)
                .build();
        return expense;
    }
}
