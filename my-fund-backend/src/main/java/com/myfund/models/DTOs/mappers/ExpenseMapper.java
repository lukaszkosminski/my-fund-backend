package com.myfund.models.DTOs.mappers;

import com.myfund.exceptions.ExpenseNotFoundException;
import com.myfund.models.DTOs.CreateExpenseDTO;
import com.myfund.models.DTOs.ExpenseDTO;
import com.myfund.models.Expense;

public class ExpenseMapper {

    public static Expense toModel(CreateExpenseDTO createExpenseDTO) {
        Expense expense = Expense.builder()
                .name(createExpenseDTO.getName())
                .amount(createExpenseDTO.getAmount())
                .idCategory(createExpenseDTO.getIdCategory())
                .idSubCategory(createExpenseDTO.getIdSubCategory()).build();
        return expense;
    }

    public static ExpenseDTO toDTO(Expense expense){
        return ExpenseDTO.builder()
                .id(expense.getId())
                .name(expense.getName())
                .amount(expense.getAmount())
                .idCategory(expense.getIdCategory())
                .idSubCategory(expense.getIdSubCategory())
                .localDate(expense.getLocalDateTime().toLocalDate())
                .build();

    }


}
