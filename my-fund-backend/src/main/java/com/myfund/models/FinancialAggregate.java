package com.myfund.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class FinancialAggregate {

    private BigDecimal value;

    private Long categoryId;

    private Long subcategoryId;

    private Long budgetId;

    private TypeAggregate typeAggregate;

    private Long userId;

}
