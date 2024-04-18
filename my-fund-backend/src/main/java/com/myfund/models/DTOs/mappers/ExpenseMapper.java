package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateExpenseDTO;
import com.myfund.models.Expense;

public class ExpenseMapper {

    public static Expense createExpenseDTOtoExpense(CreateExpenseDTO createExpenseDTO) {
        Expense expense = new Expense();
        expense.setName(createExpenseDTO.getName());
        expense.setAmount(createExpenseDTO.getAmount());
        return expense;
    }
}
