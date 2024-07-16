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

        Budget budget = BudgetMapper.createBudgetDTOMapToBudget(createBudgetDTO);

        assertNotNull(budget, "Budget should not be null");
        assertEquals("Test Budget", budget.getName(), "Budget name should match");
    }

    @Test
    void testBudgetMapToBudgetDTO() {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setName("Test Budget");
        budget.setBalance(new BigDecimal("1000.0"));
        budget.setTotalExpense(new BigDecimal("500.0"));
        budget.setTotalIncome(new BigDecimal("1500.0"));

        BudgetDTO budgetDTO = BudgetMapper.budgetMapToBudgetDTO(budget);

        assertNotNull(budgetDTO, "BudgetDTO should not be null");
        assertEquals(1L, budgetDTO.getId(), "BudgetDTO ID should match");
        assertEquals("Test Budget", budgetDTO.getName(), "BudgetDTO name should match");
        assertEquals(new BigDecimal("1000.0"), budgetDTO.getBalance(), "BudgetDTO balance should match");
        assertEquals(new BigDecimal("500.0"), budgetDTO.getTotalExpense(), "BudgetDTO total expense should match");
        assertEquals(new BigDecimal("1500.0"), budgetDTO.getTotalIncome(), "BudgetDTO total income should match");
    }

    @Test
    void testBudgetListMapToBudgetSummaryDTOList() {
        Budget budget1 = new Budget();
        budget1.setId(1L);
        budget1.setName("Budget 1");
        budget1.setBalance(new BigDecimal("1000.0"));
        budget1.setTotalExpense(new BigDecimal("500.0"));
        budget1.setTotalIncome(new BigDecimal("1500.0"));

        Budget budget2 = new Budget();
        budget2.setId(2L);
        budget2.setName("Budget 2");
        budget2.setBalance(new BigDecimal("2000.0"));
        budget2.setTotalExpense(new BigDecimal("1000.0"));
        budget2.setTotalIncome(new BigDecimal("3000.0"));

        List<Budget> budgets = Arrays.asList(budget1, budget2);
        List<BudgetSummaryDTO> budgetSummaryDTOs = BudgetMapper.budgetListMapToBudgetSummaryDTOList(budgets);

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
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setName("Test Budget");
        budget.setBalance(new BigDecimal("1000.0"));
        budget.setTotalExpense(new BigDecimal("500.0"));
        budget.setTotalIncome(new BigDecimal("1500.0"));

        BudgetSummaryDTO budgetSummaryDTO = BudgetMapper.budgetMapToBudgetSummaryDTO(budget);

        assertNotNull(budgetSummaryDTO, "BudgetSummaryDTO should not be null");
        assertEquals(1L, budgetSummaryDTO.getId(), "BudgetSummaryDTO ID should match");
        assertEquals("Test Budget", budgetSummaryDTO.getName(), "BudgetSummaryDTO name should match");
        assertEquals(new BigDecimal("1000.0"), budgetSummaryDTO.getBalance(), "BudgetSummaryDTO balance should match");
        assertEquals(new BigDecimal("500.0"), budgetSummaryDTO.getTotalExpense(), "BudgetSummaryDTO total expense should match");
        assertEquals(new BigDecimal("1500.0"), budgetSummaryDTO.getTotalIncome(), "BudgetSummaryDTO total income should match");
    }

    @Test
    void testBudgetDTOMapToBudget() {
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(1L);
        budgetDTO.setName("Test Budget");
        budgetDTO.setBalance(new BigDecimal("1000.0"));
        budgetDTO.setTotalExpense(new BigDecimal("500.0"));
        budgetDTO.setTotalIncome(new BigDecimal("1500.0"));

        Budget budget = BudgetMapper.budgetDTOMapToBudget(budgetDTO);

        assertNotNull(budget, "Budget should not be null");
        assertEquals(1L, budget.getId(), "Budget ID should match");
        assertEquals("Test Budget", budget.getName(), "Budget name should match");
        assertEquals(new BigDecimal("1000.0"), budget.getBalance(), "Budget balance should match");
        assertEquals(new BigDecimal("500.0"), budget.getTotalExpense(), "Budget total expense should match");
        assertEquals(new BigDecimal("1500.0"), budget.getTotalIncome(), "Budget total income should match");
    }
}