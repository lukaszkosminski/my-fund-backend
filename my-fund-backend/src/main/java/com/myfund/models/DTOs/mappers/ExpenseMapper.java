package com.myfund.models.DTO.mappers;

import com.myfund.models.DTO.CreateExpenseDTO;
import com.myfund.models.Expense;

public class ExpenseMapper {

    public static Expense createExpenseDTOtoExpense(CreateExpenseDTO createExpenseDTO) {
        Expense expense = new Expense();
        expense.setName(createExpenseDTO.getName());
        expense.setAmount(createExpenseDTO.getAmount());
        return expense;
    }
}
