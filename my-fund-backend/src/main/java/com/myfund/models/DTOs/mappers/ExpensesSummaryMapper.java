package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.ExpensesSummaryDTO;
import com.myfund.models.ExpensesSummary;

import java.util.stream.Collectors;

public class ExpensesSummaryMapper {

    public static ExpensesSummaryDTO toDTO(ExpensesSummary expensesSummary) {
        ExpensesSummaryDTO dto = new ExpensesSummaryDTO();
        dto.getExpensesSummary().addAll(expensesSummary.getExpensesSummary().stream()
                .map(categoryExpenses -> {
                    ExpensesSummaryDTO.CategoryExpenses categoryExpensesDTO = new ExpensesSummaryDTO.CategoryExpenses();
                    categoryExpensesDTO.setCategoryId(categoryExpenses.getCategoryId());
                    categoryExpensesDTO.setTotalExpenses(categoryExpenses.getTotalExpenses());
                    categoryExpensesDTO.setPercentageOfTotal(categoryExpenses.getPercentageOfTotal());
                    categoryExpensesDTO.setSubcategories(categoryExpenses.getSubcategories().stream()
                            .map(subcategoryExpenses -> {
                                ExpensesSummaryDTO.CategoryExpenses.SubcategoryExpenses subcategoryExpensesDTO = new ExpensesSummaryDTO.CategoryExpenses.SubcategoryExpenses();
                                subcategoryExpensesDTO.setSubcategoryId(subcategoryExpenses.getSubcategoryId());
                                subcategoryExpensesDTO.setExpenseAmount(subcategoryExpenses.getExpenseAmount());
                                return subcategoryExpensesDTO;
                            })
                            .collect(Collectors.toList()));
                    return categoryExpensesDTO;
                })
                .collect(Collectors.toList()));
        return dto;
    }
}