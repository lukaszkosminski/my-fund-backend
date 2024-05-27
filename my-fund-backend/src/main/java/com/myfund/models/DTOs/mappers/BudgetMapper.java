package com.myfund.models.DTOs.mappers;

import com.myfund.models.Budget;
import com.myfund.models.DTOs.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BudgetMapper {

    public static Budget createBudgetDTOMapToBudget(CreateBudgetDTO createBudgetDTO) {
        Budget budget = new Budget();
        budget.setName(createBudgetDTO.getName());
        return budget;
    }

    public static BudgetDTO budgetMapToBudgetDTO(Budget budget) {
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(budget.getId());
        budgetDTO.setName(budget.getName());
        budgetDTO.setBalance(budget.getBalance());
        budgetDTO.setTotalExpense(budget.getTotalExpense());
        budgetDTO.setTotalIncome(budget.getTotalIncome());
        List<ExpenseDTO> expenseDTOs = (budget.getExpenses() != null) ? budget.getExpenses().stream()
                .map(ExpenseMapper::expensetoExpenseDTO)
                .collect(Collectors.toList()) : Collections.emptyList();
        budgetDTO.setExpenses(expenseDTOs);
        List<IncomeDTO> incomeDTOs = (budget.getIncomes() != null) ? budget.getIncomes().stream()
                .map(IncomeMapper::incomeMapToIncomeDTO)
                .collect(Collectors.toList()) : Collections.emptyList();
        budgetDTO.setIncomes(incomeDTOs);
        return budgetDTO;
    }

    public static List<BudgetSummaryDTO> budgetListMapToBudgetSummaryDTOList(List<Budget> allBudgetByUser) {
        return allBudgetByUser.stream()
               .map(budget -> {
                    BudgetSummaryDTO budgetSummaryDTO = new BudgetSummaryDTO();
                    budgetSummaryDTO.setId(budget.getId());
                    budgetSummaryDTO.setName(budget.getName());
                    budgetSummaryDTO.setBalance(budget.getBalance());
                    budgetSummaryDTO.setTotalExpense(budget.getTotalExpense());
                    budgetSummaryDTO.setTotalIncome(budget.getTotalIncome());
                    return budgetSummaryDTO;
                })
               .collect(Collectors.toList());
    }
    public static BudgetSummaryDTO budgetMapToBudgetSummaryDTO(Budget budget){
        BudgetSummaryDTO budgetSummaryDTO = new BudgetSummaryDTO();
        budgetSummaryDTO.setId(budget.getId());
        budgetSummaryDTO.setName(budget.getName());
        budgetSummaryDTO.setBalance(budget.getBalance());
        budgetSummaryDTO.setTotalExpense(budget.getTotalExpense());
        budgetSummaryDTO.setTotalIncome(budget.getTotalIncome());
        return budgetSummaryDTO;
    }

    public static Budget budgetDTOMapToBudget(BudgetDTO budgetDTO) {
        Budget budget = new Budget();
        budget.setId(budgetDTO.getId());
        budget.setName(budgetDTO.getName());
        budget.setBalance(budgetDTO.getBalance());
        budget.setTotalExpense(budgetDTO.getTotalExpense());
        budget.setTotalIncome(budgetDTO.getTotalIncome());
        return budget;
    }
}
