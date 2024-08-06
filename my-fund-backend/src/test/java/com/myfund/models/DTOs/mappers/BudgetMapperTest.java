package com.myfund.models.DTOs.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.myfund.models.Budget;
import com.myfund.models.DTOs.BudgetDTO;
import com.myfund.models.DTOs.BudgetSummaryDTO;
import com.myfund.models.DTOs.CreateBudgetDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class BudgetMapperTest {

    @Test
    void testCreateBudgetDTOMapToBudget() {
        CreateBudgetDTO createBudgetDTO = new CreateBudgetDTO();
        createBudgetDTO.setName("Test Budget");

        Budget budget = BudgetMapper.toModel(createBudgetDTO);

        assertNotNull(budget, "Budget should not be null");
        assertEquals("Test Budget", budget.getName(), "Budget name should match");
    }

    @Test
    void testBudgetMapToBudgetDTO() {
        Budget budget = Budget.builder()
                .id(1L)
                .name("Test Budget")
                .balance(new BigDecimal("1000.0"))
                .totalExpense(new BigDecimal("500.0"))
                .totalIncome(new BigDecimal("1500.0"))
                .build();

        BudgetDTO budgetDTO = BudgetMapper.toDTO(budget);

        assertNotNull(budgetDTO, "BudgetDTO should not be null");
        assertEquals(1L, budgetDTO.getId(), "BudgetDTO ID should match");
        assertEquals("Test Budget", budgetDTO.getName(), "BudgetDTO name should match");
        assertEquals(new BigDecimal("1000.0"), budgetDTO.getBalance(), "BudgetDTO balance should match");
        assertEquals(new BigDecimal("500.0"), budgetDTO.getTotalExpense(), "BudgetDTO total expense should match");
        assertEquals(new BigDecimal("1500.0"), budgetDTO.getTotalIncome(), "BudgetDTO total income should match");
    }

    @Test
    void testBudgetListMapToBudgetSummaryDTOList() {
        Budget budget1 = Budget.builder()
                .id(1L)
                .name("Budget 1")
                .balance(new BigDecimal("1000.0"))
                .totalExpense(new BigDecimal("500.0"))
                .totalIncome(new BigDecimal("1500.0"))
                .build();

        Budget budget2 = Budget.builder()
                .id(2L)
                .name("Budget 2")
                .balance(new BigDecimal("2000.0"))
                .totalExpense(new BigDecimal("1000.0"))
                .totalIncome(new BigDecimal("3000.0"))
                .build();

        List<Budget> budgets = Arrays.asList(budget1, budget2);
        List<BudgetSummaryDTO> budgetSummaryDTOs = BudgetSummaryMapper.toListDTO(budgets);

        assertNotNull(budgetSummaryDTOs, "BudgetSummaryDTO list should not be null");
        assertEquals(2, budgetSummaryDTOs.size(), "BudgetSummaryDTO list size should match");

        BudgetSummaryDTO summaryDTO1 = budgetSummaryDTOs.get(0);
        assertEquals(1L, summaryDTO1.getId(), "BudgetSummaryDTO ID should match");
        assertEquals("Budget 1", summaryDTO1.getName(), "BudgetSummaryDTO name should match");
        assertEquals(new BigDecimal("1000.0"), summaryDTO1.getBalance(), "BudgetSummaryDTO balance should match");
        assertEquals(new BigDecimal("500.0"), summaryDTO1.getTotalExpense(), "BudgetSummaryDTO total expense should match");
        assertEquals(new BigDecimal("1500.0"), summaryDTO1.getTotalIncome(), "BudgetSummaryDTO total income should match");

        BudgetSummaryDTO summaryDTO2 = budgetSummaryDTOs.get(1);
        assertEquals(2L, summaryDTO2.getId(), "BudgetSummaryDTO ID should match");
        assertEquals("Budget 2", summaryDTO2.getName(), "BudgetSummaryDTO name should match");
        assertEquals(new BigDecimal("2000.0"), summaryDTO2.getBalance(), "BudgetSummaryDTO balance should match");
        assertEquals(new BigDecimal("1000.0"), summaryDTO2.getTotalExpense(), "BudgetSummaryDTO total expense should match");
        assertEquals(new BigDecimal("3000.0"), summaryDTO2.getTotalIncome(), "BudgetSummaryDTO total income should match");
    }

    @Test
    void testBudgetMapToBudgetSummaryDTO() {
        Budget budget = Budget.builder()
                .id(1L)
                .name("Test Budget")
                .balance(new BigDecimal("1000.0"))
                .totalExpense(new BigDecimal("500.0"))
                .totalIncome(new BigDecimal("1500.0"))
                .build();

        BudgetSummaryDTO budgetSummaryDTO = BudgetSummaryMapper.toDTO(budget);

        assertNotNull(budgetSummaryDTO, "BudgetSummaryDTO should not be null");
        assertEquals(1L, budgetSummaryDTO.getId(), "BudgetSummaryDTO ID should match");
        assertEquals("Test Budget", budgetSummaryDTO.getName(), "BudgetSummaryDTO name should match");
        assertEquals(new BigDecimal("1000.0"), budgetSummaryDTO.getBalance(), "BudgetSummaryDTO balance should match");
        assertEquals(new BigDecimal("500.0"), budgetSummaryDTO.getTotalExpense(), "BudgetSummaryDTO total expense should match");
        assertEquals(new BigDecimal("1500.0"), budgetSummaryDTO.getTotalIncome(), "BudgetSummaryDTO total income should match");
    }

    @Test
    void testBudgetDTOMapToBudget() {
        BudgetDTO budgetDTO = BudgetDTO.builder()
                .id(1L)
                .name("Test Budget")
                .balance(new BigDecimal("1000.0"))
                .totalExpense(new BigDecimal("500.0"))
                .totalIncome(new BigDecimal("1500.0"))
                .build();

        Budget budget = BudgetMapper.toModelFromDTO(budgetDTO);

        assertNotNull(budget, "Budget should not be null");
        assertEquals(1L, budget.getId(), "Budget ID should match");
        assertEquals("Test Budget", budget.getName(), "Budget name should match");
        assertEquals(new BigDecimal("1000.0"), budget.getBalance(), "Budget balance should match");
        assertEquals(new BigDecimal("500.0"), budget.getTotalExpense(), "Budget total expense should match");
        assertEquals(new BigDecimal("1500.0"), budget.getTotalIncome(), "Budget total income should match");
    }
}