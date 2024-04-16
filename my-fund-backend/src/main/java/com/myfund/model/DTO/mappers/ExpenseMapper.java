package com.myfund.model.DTO.mappers;

import com.myfund.model.DTO.CreateExpenseDTO;
import com.myfund.model.Expense;

public class ExpenseMapper {

    public static Expense createExpenseDTOtoExpense(CreateExpenseDTO createExpenseDTO) {
        Expense expense = new Expense();
        expense.setName(createExpenseDTO.getName());
        expense.setAmount(createExpenseDTO.getAmount());
        return expense;
    }
}
