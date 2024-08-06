package com.myfund.models;

import com.myfund.models.DTOs.ExpensesSummaryDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Getter
public class ExpensesSummary {

    private final List<ExpensesSummary.CategoryExpenses> expensesSummary;

    public ExpensesSummary() {
        this.expensesSummary = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class CategoryExpenses {
        private Long categoryId;
        private BigDecimal totalExpenses = BigDecimal.ZERO;
        private List<ExpensesSummary.CategoryExpenses.SubcategoryExpenses> subcategories;
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
