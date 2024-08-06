package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.FinancialAggregateDTO;
import com.myfund.models.FinancialAggregate;

public class FinancialAggregateMapper {
    public static FinancialAggregateDTO toDTO(FinancialAggregate financialAggregate) {
        return FinancialAggregateDTO.builder()
                .value(financialAggregate.getValue())
                .categoryId(financialAggregate.getCategoryId())
                .subcategoryId(financialAggregate.getSubcategoryId())
                .typeAggregate(financialAggregate.getTypeAggregate())
                .budgetId(financialAggregate.getBudgetId())
                .userId(financialAggregate.getUserId())
                .build();
    }

}
