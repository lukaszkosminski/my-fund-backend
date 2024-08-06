package com.myfund.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class BudgetSummaryDTO {

    private Long id;

    private String name;

    private BigDecimal balance;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

}
