package com.myfund.models.DTOs;

import com.myfund.models.TypeAggregate;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Data
@Builder
public class FinancialAggregateDTO {

    private BigDecimal value;

    private Long subcategoryId;

    private Long categoryId;

    private Long budgetId;

    private TypeAggregate typeAggregate;

    private Long userId;

}
