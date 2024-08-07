package com.myfund.models.DTOs;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BudgetDTO {

    private Long id;

    private String name;

    @Builder.Default private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default private BigDecimal totalIncome = BigDecimal.ZERO;

    @Builder.Default private BigDecimal totalExpense = BigDecimal.ZERO;

    private List<ExpenseDTO> expenses;

    private List<IncomeDTO> incomes;
}
