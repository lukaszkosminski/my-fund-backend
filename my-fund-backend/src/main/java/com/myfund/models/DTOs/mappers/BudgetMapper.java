package com.myfund.models.DTO.mappers;

import com.myfund.models.Budget;
import com.myfund.models.DTO.BudgetDTO;
import com.myfund.models.DTO.CreateBudgetDTO;
import com.myfund.models.DTO.EditBudgetDTO;

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
