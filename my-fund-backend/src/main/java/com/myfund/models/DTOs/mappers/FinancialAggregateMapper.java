package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.FinancialAggregateCategoryDTO;
import com.myfund.models.DTOs.FinancialAggregateSubcategoryDTO;
import com.myfund.models.FinancialAggregate;

public class FinancialAggregateMapper {
    public static FinancialAggregateCategoryDTO toFinancialAggregateCategoryDTO(FinancialAggregate financialAggregate) {
        FinancialAggregateCategoryDTO financialAggregateCategoryDTO = new FinancialAggregateCategoryDTO();
        financialAggregateCategoryDTO.setValue(financialAggregate.getValue());
        financialAggregateCategoryDTO.setCategoryId(financialAggregate.getCategoryId());
        financialAggregateCategoryDTO.setTypeAggregate(financialAggregate.getTypeAggregate());
        financialAggregateCategoryDTO.setBudgetId(financialAggregate.getBudgetId());
        financialAggregateCategoryDTO.setUserId(financialAggregate.getUserId());
        financialAggregateCategoryDTO.setValue(financialAggregate.getValue());
        return financialAggregateCategoryDTO;
    }

    public static FinancialAggregateSubcategoryDTO toFinancialAggregateSubcategoryDTO(FinancialAggregate financialAggregate) {
        FinancialAggregateSubcategoryDTO financialAggregateSubcategoryDTO = new FinancialAggregateSubcategoryDTO();
        financialAggregateSubcategoryDTO.setValue(financialAggregate.getValue());
        financialAggregateSubcategoryDTO.setSubcategoryId(financialAggregate.getSubcategoryId());
        financialAggregateSubcategoryDTO.setTypeAggregate(financialAggregate.getTypeAggregate());
        financialAggregateSubcategoryDTO.setBudgetId(financialAggregate.getBudgetId());
        financialAggregateSubcategoryDTO.setUserId(financialAggregate.getUserId());
        financialAggregateSubcategoryDTO.setValue(financialAggregate.getValue());
        return financialAggregateSubcategoryDTO;
    }
}
