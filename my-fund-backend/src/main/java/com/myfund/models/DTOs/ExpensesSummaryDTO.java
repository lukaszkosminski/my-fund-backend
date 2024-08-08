package com.myfund.models.DTOs;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ExpensesSummaryDTO {

    private final List<CategoryExpenses> expensesSummary;

    public ExpensesSummaryDTO() {
        this.expensesSummary = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class CategoryExpenses {
        private Long categoryId;
        private BigDecimal totalExpenses = BigDecimal.ZERO;
        private List<SubcategoryExpenses> subcategories;
        private BigDecimal percentageOfTotal;

        public CategoryExpenses() {
            this.subcategories = new ArrayList<>();
        }

        @Getter
        @Setter
        public static class SubcategoryExpenses {
            private Long subcategoryId;
            private BigDecimal expenseAmount;
        }
    }
}
