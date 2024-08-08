package com.myfund.models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FinancialAggregate {

    private BigDecimal value;

    private Long categoryId;

    private Long subcategoryId;

    private Long budgetId;

    private TypeAggregate typeAggregate;

    private Long userId;

    public static FinancialAggregate createByCategory(BigDecimal value, Long categoryId,Long budgetId, TypeAggregate typeAggregate, Long userId) {
        return FinancialAggregate.builder()
               .value(value)
               .categoryId(categoryId)
               .budgetId(budgetId)
               .typeAggregate(typeAggregate)
               .userId(userId)
               .build();
    }

    public static FinancialAggregate createBySubcategory(BigDecimal value, Long subcategoryId, Long budgetId, TypeAggregate typeAggregate, Long userId) {
        return FinancialAggregate.builder()
               .value(value)
               .subcategoryId(subcategoryId)
               .budgetId(budgetId)
               .typeAggregate(typeAggregate)
               .userId(userId)
               .build();
    }

}
