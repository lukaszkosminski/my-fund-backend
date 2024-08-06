package com.myfund.models.DTOs.mappers;

import com.myfund.models.Budget;
import com.myfund.models.DTOs.BudgetSummaryDTO;

import java.util.List;
import java.util.stream.Collectors;

public class BudgetSummaryMapper {


    public static List<BudgetSummaryDTO> toListDTO(List<Budget> allBudgetByUser) {
        return allBudgetByUser.stream()
                .map(budget -> {
                    BudgetSummaryDTO budgetSummaryDTO = BudgetSummaryDTO.builder()
                            .id(budget.getId())
                            .name(budget.getName())
                            .balance(budget.getBalance())
                            .totalExpense(budget.getTotalExpense())
                            .totalIncome(budget.getTotalIncome())
                            .build();

                    return budgetSummaryDTO;
                })
                .collect(Collectors.toList());
    }
    public static BudgetSummaryDTO toDTO(Budget budget){
        BudgetSummaryDTO budgetSummaryDTO = BudgetSummaryDTO.builder()
                .id(budget.getId())
                .name(budget.getName())
                .balance(budget.getBalance())
                .totalExpense(budget.getTotalExpense())
                .totalIncome(budget.getTotalIncome())
                .build();
        return budgetSummaryDTO;
    }
}
