package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.FinancialAggregateDTO;
import com.myfund.models.FinancialAggregate;
import com.myfund.models.TypeAggregate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinancialAggregateMapperTest {

    @Test
    void testToFinancialAggregateCategoryDTO() {
        FinancialAggregate financialAggregate = FinancialAggregate.builder()
                .value(new BigDecimal("100.0"))
                .categoryId(1L)
                .typeAggregate(TypeAggregate.EXPENSES_BY_CATEGORY)
                .budgetId(2L)
                .userId(3L)
                .build();
        FinancialAggregateDTO dto = FinancialAggregateMapper.toDTO(financialAggregate);

        assertEquals(new BigDecimal("100.0"), dto.getValue());
        assertEquals(1L, dto.getCategoryId());
        assertEquals(TypeAggregate.EXPENSES_BY_CATEGORY, dto.getTypeAggregate());
        assertEquals(2L, dto.getBudgetId());
        assertEquals(3L, dto.getUserId());
    }

    @Test
    void testToFinancialAggregateSubcategoryDTO() {
        FinancialAggregate financialAggregate = FinancialAggregate.builder()
                .value(new BigDecimal("200.0"))
                .subcategoryId(4L)
                .typeAggregate(TypeAggregate.EXPENSES_BY_CATEGORY)
                .budgetId(5L)
                .userId(6L)
                .build();

        FinancialAggregateDTO dto = FinancialAggregateMapper.toDTO(financialAggregate);

        assertEquals(new BigDecimal("200.0"), dto.getValue());
        assertEquals(4L, dto.getSubcategoryId());
        assertEquals(TypeAggregate.EXPENSES_BY_CATEGORY, dto.getTypeAggregate());
        assertEquals(5L, dto.getBudgetId());
        assertEquals(6L, dto.getUserId());
    }

    @Test
    void testToFinancialAggregateCategoryDTO_OverrideValue() {
        FinancialAggregate financialAggregate = FinancialAggregate.builder()
                .value(new BigDecimal("300.0"))
                .categoryId(7L)
                .typeAggregate(TypeAggregate.EXPENSES_BY_CATEGORY)
                .budgetId(8L)
                .userId(9L)
                .build();

        FinancialAggregateDTO dto = FinancialAggregateMapper.toDTO(financialAggregate);
        dto.setValue(new BigDecimal("400.0"));

        assertEquals(new BigDecimal("400.0"), dto.getValue());
        assertEquals(7L, dto.getCategoryId());
        assertEquals(TypeAggregate.EXPENSES_BY_CATEGORY, dto.getTypeAggregate());
        assertEquals(8L, dto.getBudgetId());
        assertEquals(9L, dto.getUserId());
    }
}