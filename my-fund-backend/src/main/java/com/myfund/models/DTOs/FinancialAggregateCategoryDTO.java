package com.myfund.models.DTOs;

import com.myfund.models.TypeAggregate;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FinancialAggregateCategoryDTO {

    private BigDecimal value;

    private Long categoryId;

    private Long budgetId;

    private TypeAggregate typeAggregate;

    private Long userId;

}
