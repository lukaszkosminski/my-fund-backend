package com.myfund.models.DTOs.mappers;

import com.myfund.models.Budget;
import com.myfund.models.DTOs.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BudgetMapper {

    public static Budget toModel(CreateBudgetDTO createBudgetDTO) {
        Budget budget = Budget.builder().name(createBudgetDTO.getName()).build();
        return budget;
    }

    public static BudgetDTO toDTO(Budget budget) {
        return BudgetDTO.builder()
                .id(budget.getId())
                .name(budget.getName())
                .balance(budget.getBalance())
                .totalExpense(budget.getTotalExpense())
                .totalIncome(budget.getTotalIncome())
                .expenses((budget.getExpenses() != null) ? budget.getExpenses().stream()
                        .map(ExpenseMapper::toDTO)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .incomes((budget.getIncomes() != null) ? budget.getIncomes().stream()
                        .map(IncomeMapper::toDTO)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }

    public static Budget toModelFromDTO(BudgetDTO budgetDTO) {
        Budget budget = Budget.builder()
                .id(budgetDTO.getId())
                .name(budgetDTO.getName())
                .totalIncome(budgetDTO.getTotalIncome())
                .totalExpense(budgetDTO.getTotalExpense())
                .balance(budgetDTO.getBalance())
                .build();
        return budget;
    }
}
