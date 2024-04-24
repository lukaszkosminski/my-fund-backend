package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateExpenseDTO;
import com.myfund.models.DTOs.ExpenseDTO;
import com.myfund.models.Expense;

public class ExpenseMapper {

    public static Expense createExpenseDTOtoExpense(CreateExpenseDTO createExpenseDTO) {
        Expense expense = new Expense();
        expense.setName(createExpenseDTO.getName());
        expense.setAmount(createExpenseDTO.getAmount());
        expense.setIdCategory(createExpenseDTO.getIdCategory());
        expense.setIdSubCategory(createExpenseDTO.getIdSubCategory());
        return expense;
    }

    public static ExpenseDTO expensetoExpenseDTO(Expense expense){
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setId(expense.getId());
        expenseDTO.setName(expense.getName());
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setIdCategory(expense.getIdCategory());
        expenseDTO.setIdSubCategory(expense.getIdSubCategory());
        return expenseDTO;
    }


}
