package com.myfund.controllers;

import com.myfund.models.DTOs.*;
import com.myfund.models.User;
import com.myfund.services.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/")
public class BudgetController {

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/budgets")
    public ResponseEntity<BudgetDTO> createBudget(@RequestBody CreateBudgetDTO createBudgetDTO, @AuthenticationPrincipal User user) {
        BudgetDTO budgetDTO = budgetService.createBudget(createBudgetDTO, user);
        return new ResponseEntity<>(budgetDTO, HttpStatus.CREATED);
    }

    @GetMapping("/budgets/{budgetId}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable("budgetId") Long budgetId, @AuthenticationPrincipal User user) {
        BudgetDTO budgetDTO = budgetService.findBudgetByIdAndUser(budgetId, user);
        return new ResponseEntity<>(budgetDTO, HttpStatus.OK);
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetSummaryDTO>> getAllBudgets(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(budgetService.findAllBudgetsByUser(user), HttpStatus.OK);
    }

    @PostMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<ExpenseDTO> createExpense(@PathVariable("budgetId") Long budgetId, @RequestBody CreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal User user) {
        ExpenseDTO expenseDTO = budgetService.createExpense(budgetId, createExpenseDTO, user);
        return new ResponseEntity<>(expenseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/budgets/{budgetId}/incomes")
    public ResponseEntity<IncomeDTO> createIncome(@PathVariable("budgetId") Long budgetId, @RequestBody CreateIncomeDTO createIncomeDTO, @AuthenticationPrincipal User user) {
        IncomeDTO incomeDTO = budgetService.createIncome(budgetId, createIncomeDTO, user);
        return new ResponseEntity<>(incomeDTO, HttpStatus.CREATED);
    }

    @PatchMapping("/budgets/{budgetId}/expenses/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable("budgetId") Long budgetId, @PathVariable("expenseId") Long expenseId, @RequestBody CreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal User user) {
        ExpenseDTO expenseDTO = budgetService.updateExpense(budgetId, expenseId, createExpenseDTO, user);
        return new ResponseEntity<>(expenseDTO, HttpStatus.OK);
    }

    @PatchMapping("/budgets/{budgetId}/incomes/{incomeId}")
    public ResponseEntity<IncomeDTO> updateIncome(@PathVariable("budgetId") Long budgetId, @PathVariable("incomeId") Long incomeId, @RequestBody CreateIncomeDTO createIncomeDTO, @AuthenticationPrincipal User user) {
        IncomeDTO incomeDTO = budgetService.updateIncome(budgetId, incomeId, createIncomeDTO, user);
        return new ResponseEntity<>(incomeDTO, HttpStatus.OK);
    }

    @GetMapping("/budgets/{budgetId}/categories/{categoryId}/expenses/total")
    public ResponseEntity<FinancialAggregateCategoryDTO> getTotalExpensesForBudgetAndCategory(@PathVariable("budgetId") Long budgetId, @PathVariable("categoryId") Long categoryId, @AuthenticationPrincipal User user) {
        FinancialAggregateCategoryDTO totalExpensesByCategory = budgetService.getTotalExpensesByCategory(budgetId, categoryId, user);
        return new ResponseEntity<>(totalExpensesByCategory, HttpStatus.OK);
    }

    @GetMapping("/budgets/{budgetId}/subcategories/{subcategoryId}/expenses/total")
    public ResponseEntity<FinancialAggregateSubcategoryDTO> getTotalExpensesForBudgetAndSubcategory(@PathVariable("budgetId") Long budgetId, @PathVariable("subcategoryId") Long subcategoryId, @AuthenticationPrincipal User user) {
        FinancialAggregateSubcategoryDTO totalExpensesBySubcategory = budgetService.getTotalExpensesBySubcategory(budgetId, subcategoryId, user);
        return new ResponseEntity<>(totalExpensesBySubcategory, HttpStatus.OK);
    }

    @GetMapping("/budgets/{budgetId}/categories/{categoryId}/incomes/total")
    public ResponseEntity<FinancialAggregateCategoryDTO> getTotalIncomesForBudgetAndCategory(@PathVariable("budgetId") Long budgetId, @PathVariable("categoryId") Long categoryId, @AuthenticationPrincipal User user) {
        FinancialAggregateCategoryDTO totalIncomesByCategory = budgetService.getTotalIncomesByCategory(budgetId, categoryId, user);
        return new ResponseEntity<>(totalIncomesByCategory, HttpStatus.OK);
    }

    @GetMapping("/budgets/{budgetId}/subcategories/{subcategoryId}/incomes/total")
    public ResponseEntity<FinancialAggregateSubcategoryDTO> getTotalIncomesForBudgetAndSubcategory(@PathVariable("budgetId") Long budgetId, @PathVariable("subcategoryId") Long subcategoryId, @AuthenticationPrincipal User user) {
        FinancialAggregateSubcategoryDTO totalIncomesBySubcategory = budgetService.getTotalIncomesBySubcategory(budgetId, subcategoryId, user);
        return new ResponseEntity<>(totalIncomesBySubcategory, HttpStatus.OK);
    }

    @DeleteMapping("/budgets/{budgetId}")
    public ResponseEntity<?> deleteBudget(@PathVariable("budgetId") Long budgetId, @AuthenticationPrincipal User user) {
        budgetService.deleteBudgetByIdAndUser(budgetId, user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}