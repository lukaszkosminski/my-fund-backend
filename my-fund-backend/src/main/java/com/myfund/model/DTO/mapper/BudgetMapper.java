package com.myfund.model.DTO.mapper;

import com.myfund.model.Budget;
import com.myfund.model.DTO.BudgetDTO;
import com.myfund.model.DTO.CreateBudgetDTO;
import com.myfund.model.DTO.EditBudgetDTO;

public class BudgetMapper {

    public static Budget createBudgetDTOMapToBudget(CreateBudgetDTO createBudgetDTO) {
        Budget budget = new Budget();
        budget.setNameBudget(createBudgetDTO.getNameBudget());
        return budget;
    }

    public static Budget editBudgetDTOMapToBudget(EditBudgetDTO editBudgetDTO) {
        Budget budget = new Budget();
        budget.setNameBudget(editBudgetDTO.getNameBudget());
        return budget;
    }

    public static BudgetDTO budgetMapToBudgetDTO(Budget budget) {
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setNameBudget(budget.getNameBudget());
        return budgetDTO;
    }
}
