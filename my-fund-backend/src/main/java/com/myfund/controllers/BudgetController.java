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
import java.util.Optional;

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
        Optional<BudgetDTO> budgetOpt = budgetService.createBudget(createBudgetDTO, user);
        if (budgetOpt.isPresent()) {
            return new ResponseEntity<>(budgetOpt.get(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/budgets/{budgetId}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable("budgetId") Long budgetId, @AuthenticationPrincipal User user) {
        Optional<BudgetDTO> budgetOpt = budgetService.findBudgetByIdAndUser(budgetId, user);
        if (budgetOpt.isPresent()) {
            return new ResponseEntity<>(budgetOpt.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetSummaryDTO>> getAllBudgets(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(budgetService.findAllBudgetsByUser(user), HttpStatus.OK);
    }

    @PostMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<ExpenseDTO> createExpense(@PathVariable("budgetId") Long budgetId, @RequestBody CreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal User user) {
        Optional<ExpenseDTO> expenseOpt = budgetService.createExpense(budgetId, createExpenseDTO, user);
        if (expenseOpt.isPresent()) {
            return new ResponseEntity<>(expenseOpt.get(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/budgets/{budgetId}/incomes")
    public ResponseEntity<IncomeDTO> createIncome(@PathVariable("budgetId") Long budgetId, @RequestBody CreateIncomeDTO createIncomeDTO, @AuthenticationPrincipal User user) {
        Optional<IncomeDTO> incomeOpt = budgetService.createIncome(budgetId, createIncomeDTO, user);
        if (incomeOpt.isPresent()) {
            return new ResponseEntity<>(incomeOpt.get(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/budgets/{budgetId}/expenses/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable("budgetId") Long budgetId, @PathVariable("expenseId") Long expenseId, @RequestBody CreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal User user) {
        Optional<ExpenseDTO> updatedExpenseOpt = budgetService.updateExpense(expenseId, createExpenseDTO, user);
        if (updatedExpenseOpt.isPresent()) {
            return new ResponseEntity<>(updatedExpenseOpt.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/budgets/{budgetId}/incomes/{incomeId}")
    public ResponseEntity<IncomeDTO> updateIncome(@PathVariable("budgetId") Long budgetId, @PathVariable("incomeId") Long incomeId, @RequestBody CreateIncomeDTO createIncomeDTO, @AuthenticationPrincipal User user) {
        Optional<IncomeDTO> updatedIncomeOpt = budgetService.updateIncome(incomeId, createIncomeDTO, user);
        if (updatedIncomeOpt.isPresent()) {
            return new ResponseEntity<>(updatedIncomeOpt.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}