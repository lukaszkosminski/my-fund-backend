package com.myfund.models.DTOs.mappers;

import com.myfund.models.Budget;
import com.myfund.models.DTOs.BudgetDTO;
import com.myfund.models.DTOs.CreateBudgetDTO;
import com.myfund.models.DTOs.EditBudgetDTO;

public class BudgetMapper {

    public static Budget createBudgetDTOMapToBudget(CreateBudgetDTO createBudgetDTO) {
        Budget budget = new Budget();
        budget.setName(createBudgetDTO.getName());
        return budget;
    }

    public static Budget editBudgetDTOMapToBudget(EditBudgetDTO editBudgetDTO) {
        Budget budget = new Budget();
        budget.setName(editBudgetDTO.getName());
        return budget;
    }

    public static BudgetDTO budgetMapToBudgetDTO(Budget budget) {
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setName(budget.getName());
        return budgetDTO;
    }
}
