package com.myfund.controllers;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.*;
import com.myfund.models.DTOs.*;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.models.DTOs.mappers.ExpenseMapper;
import com.myfund.models.DTOs.mappers.IncomeMapper;
import com.myfund.services.BudgetService;
import com.myfund.services.csv.CsvReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/")
public class BudgetController {

    private final BudgetService budgetService;

    private final CsvReaderService csvReaderService;

    @Autowired
    public BudgetController(BudgetService budgetService, CsvReaderService csvReaderService) {
        this.budgetService = budgetService;
        this.csvReaderService = csvReaderService;
    }

    @PostMapping("/budgets")
    public ResponseEntity<BudgetDTO> createBudget(@RequestBody @Valid CreateBudgetDTO createBudgetDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        Budget budget = budgetService.createBudget(BudgetMapper.toBudget(createBudgetDTO), user);
        return new ResponseEntity<>(BudgetMapper.toBudgetDTO(budget), HttpStatus.CREATED);
    }

    @GetMapping("/budgets/{budgetId}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable("budgetId") Long budgetId, @AuthenticationPrincipal User user) {
        Budget budget = budgetService.findBudgetByIdAndUser(budgetId, user);
        return new ResponseEntity<>(BudgetMapper.toBudgetDTO(budget), HttpStatus.OK);
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetSummaryDTO>> getAllBudgets(@AuthenticationPrincipal User user) {
        List<Budget> allBudgetsByUser = budgetService.findAllBudgetsByUser(user);
        return new ResponseEntity<>(BudgetMapper.toListBudgetSummaryDTO(allBudgetsByUser), HttpStatus.OK);
    }

    @PostMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<ExpenseDTO> createExpense(@PathVariable("budgetId") Long budgetId, @RequestBody @Valid CreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        Expense expense = budgetService.createExpense(budgetId, ExpenseMapper.toExpense(createExpenseDTO), user);
        return new ResponseEntity<>(ExpenseMapper.toExpenseDTO(expense), HttpStatus.CREATED);
    }

    @PostMapping("/budgets/{budgetId}/incomes")
    public ResponseEntity<IncomeDTO> createIncome(@PathVariable("budgetId") Long budgetId, @RequestBody @Valid CreateIncomeDTO createIncomeDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        Income income = budgetService.createIncome(budgetId, IncomeMapper.toIncome(createIncomeDTO), user);
        return new ResponseEntity<>(IncomeMapper.toIncomeDTO(income), HttpStatus.CREATED);
    }

    @PatchMapping("/budgets/{budgetId}/expenses/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable("budgetId") Long budgetId, @PathVariable("expenseId") Long expenseId, @RequestBody CreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        ExpenseDTO expenseDTO = budgetService.updateExpense(budgetId, expenseId, createExpenseDTO, user);
        return new ResponseEntity<>(expenseDTO, HttpStatus.OK);
    }

    @PatchMapping("/budgets/{budgetId}/incomes/{incomeId}")
    public ResponseEntity<IncomeDTO> updateIncome(@PathVariable("budgetId") Long budgetId, @PathVariable("incomeId") Long incomeId, @RequestBody CreateIncomeDTO createIncomeDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
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
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/budgets/{budgetId}/expenses/{expenseId}")
    public ResponseEntity<?> deleteExpense(@PathVariable("budgetId") Long budgetId, @PathVariable("expenseId") Long expenseId, @AuthenticationPrincipal User user) {
        budgetService.deleteExpenseByIdAndUser(expenseId, user, budgetId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/budgets/{budgetId}/incomes/{incomeId}")
    public ResponseEntity<?> deleteIncome(@PathVariable("budgetId") Long budgetId, @PathVariable("incomeId") Long incomeId, @AuthenticationPrincipal User user) {
        budgetService.deleteIncomeByIdAndUser(incomeId, user, budgetId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/budgets/{budgetId}/expenses/summary")
    public ResponseEntity<ExpensesSummaryDTO> calculateExpensesSummary(@PathVariable Long budgetId, @AuthenticationPrincipal User user) {
        ExpensesSummaryDTO expensesSummaryDTO = budgetService.calculateExpensesSummary(user, budgetId);
        return new ResponseEntity<>(expensesSummaryDTO, HttpStatus.OK);
    }

    @PostMapping("/budgets/{budgetId}/upload-csv/{bankName}")
    public ResponseEntity<String> uploadCsv(@PathVariable(value = "bankName") BankName bankName, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user, @PathVariable Long budgetId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"message\": \"File is empty\"}");
        }
        try {
            csvReaderService.parseFile(bankName, file, user, budgetId);
            String successMessage = String.format("Successfully uploaded '%s' for %s", file.getOriginalFilename(), bankName);
            return ResponseEntity.ok("{\"message\": \"" + successMessage + "\"}");
        } catch (Exception e) {
            String errorMessage = String.format("Error processing file: %s", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"" + errorMessage + "\"}");
        }
    }
}