package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.FinancialAggregateCategoryDTO;
import com.myfund.models.DTOs.FinancialAggregateSubcategoryDTO;
import com.myfund.models.FinancialAggregate;
import com.myfund.models.TypeAggregate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinancialAggregateMapperTest {

    @Test
    void testToFinancialAggregateCategoryDTO() {
        FinancialAggregate financialAggregate = new FinancialAggregate();
        financialAggregate.setValue(new BigDecimal("100.0"));
        financialAggregate.setCategoryId(1L);
        financialAggregate.setTypeAggregate(TypeAggregate.EXPENSES_BY_CATEGORY);
        financialAggregate.setBudgetId(2L);
        financialAggregate.setUserId(3L);

        FinancialAggregateCategoryDTO dto = FinancialAggregateMapper.toFinancialAggregateCategoryDTO(financialAggregate);

        assertEquals(new BigDecimal("100.0"), dto.getValue());
        assertEquals(1L, dto.getCategoryId());
        assertEquals(TypeAggregate.EXPENSES_BY_CATEGORY, dto.getTypeAggregate());
        assertEquals(2L, dto.getBudgetId());
        assertEquals(3L, dto.getUserId());
    }

    @Test
    void testToFinancialAggregateSubcategoryDTO() {
        FinancialAggregate financialAggregate = new FinancialAggregate();
        financialAggregate.setValue(new BigDecimal("200.0"));
        financialAggregate.setSubcategoryId(4L);
        financialAggregate.setTypeAggregate(TypeAggregate.EXPENSES_BY_CATEGORY);
        financialAggregate.setBudgetId(5L);
        financialAggregate.setUserId(6L);

        FinancialAggregateSubcategoryDTO dto = FinancialAggregateMapper.toFinancialAggregateSubcategoryDTO(financialAggregate);

        assertEquals(new BigDecimal("200.0"), dto.getValue());
        assertEquals(4L, dto.getSubcategoryId());
        assertEquals(TypeAggregate.EXPENSES_BY_CATEGORY, dto.getTypeAggregate());
        assertEquals(5L, dto.getBudgetId());
        assertEquals(6L, dto.getUserId());
    }

    @Test
    void testToFinancialAggregateCategoryDTO_OverrideValue() {
        FinancialAggregate financialAggregate = new FinancialAggregate();
        financialAggregate.setValue(new BigDecimal("300.0"));
        financialAggregate.setCategoryId(7L);
        financialAggregate.setTypeAggregate(TypeAggregate.EXPENSES_BY_CATEGORY);
        financialAggregate.setBudgetId(8L);
        financialAggregate.setUserId(9L);

        FinancialAggregateCategoryDTO dto = FinancialAggregateMapper.toFinancialAggregateCategoryDTO(financialAggregate);
        dto.setValue(new BigDecimal("400.0"));

        assertEquals(new BigDecimal("400.0"), dto.getValue());
        assertEquals(7L, dto.getCategoryId());
        assertEquals(TypeAggregate.EXPENSES_BY_CATEGORY, dto.getTypeAggregate());
        assertEquals(8L, dto.getBudgetId());
        assertEquals(9L, dto.getUserId());
    }
}