package com.myfund.integration;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.*;
import com.myfund.models.DTOs.*;
import com.myfund.models.DTOs.mappers.*;
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
        Budget budget = budgetService.createBudget(BudgetMapper.toModel(createBudgetDTO), user);
        return new ResponseEntity<>(BudgetMapper.toDTO(budget), HttpStatus.CREATED);
    }

    @GetMapping("/budgets/{budgetId}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable("budgetId") Long budgetId, @AuthenticationPrincipal User user) {
        Budget budget = budgetService.findBudgetByIdAndUser(budgetId, user);
        return new ResponseEntity<>(BudgetMapper.toDTO(budget), HttpStatus.OK);
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetSummaryDTO>> getAllBudgets(@AuthenticationPrincipal User user) {
        List<Budget> allBudgetsByUser = budgetService.findAllBudgetsByUser(user);
        return new ResponseEntity<>(BudgetSummaryMapper.toListDTO(allBudgetsByUser), HttpStatus.OK);
    }

    @PostMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<ExpenseDTO> createExpense(@PathVariable("budgetId") Long budgetId, @RequestBody @Valid CreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        Expense expense = budgetService.createExpense(budgetId, ExpenseMapper.toModel(createExpenseDTO), user);
        return new ResponseEntity<>(ExpenseMapper.toDTO(expense), HttpStatus.CREATED);
    }

    @PostMapping("/budgets/{budgetId}/incomes")
    public ResponseEntity<IncomeDTO> createIncome(@PathVariable("budgetId") Long budgetId, @RequestBody @Valid CreateIncomeDTO createIncomeDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        Income income = budgetService.createIncome(budgetId, IncomeMapper.toModel(createIncomeDTO), user);
        return new ResponseEntity<>(IncomeMapper.toDTO(income), HttpStatus.CREATED);
    }

    @PatchMapping("/budgets/{budgetId}/expenses/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable("budgetId") Long budgetId, @PathVariable("expenseId") Long expenseId, @RequestBody @Valid CreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        Expense expense = budgetService.updateExpense(budgetId, expenseId,ExpenseMapper.toModel(createExpenseDTO), user);
        return new ResponseEntity<>(ExpenseMapper.toDTO(expense), HttpStatus.OK);
    }

    @PatchMapping("/budgets/{budgetId}/incomes/{incomeId}")
    public ResponseEntity<IncomeDTO> updateIncome(@PathVariable("budgetId") Long budgetId, @PathVariable("incomeId") Long incomeId, @RequestBody @Valid CreateIncomeDTO createIncomeDTO, @AuthenticationPrincipal User user) throws InvalidInputException {
        Income income = budgetService.updateIncome(budgetId, incomeId, IncomeMapper.toModel(createIncomeDTO), user);
        return new ResponseEntity<>(IncomeMapper.toDTO(income), HttpStatus.OK);
    }

    @GetMapping("/budgets/{budgetId}/categories/{categoryId}/expenses/total")
    public ResponseEntity<FinancialAggregateDTO> getTotalExpensesForBudgetAndCategory(@PathVariable("budgetId") Long budgetId, @PathVariable("categoryId") Long categoryId, @AuthenticationPrincipal User user) {
        FinancialAggregate totalExpensesByCategory = budgetService.getTotalExpensesByCategory(budgetId, categoryId, user);
        return new ResponseEntity<>(FinancialAggregateMapper.toDTO(totalExpensesByCategory), HttpStatus.OK);
    }

    @GetMapping("/budgets/{budgetId}/subcategories/{subcategoryId}/expenses/total")
    public ResponseEntity<FinancialAggregateDTO> getTotalExpensesForBudgetAndSubcategory(@PathVariable("budgetId") Long budgetId, @PathVariable("subcategoryId") Long subcategoryId, @AuthenticationPrincipal User user) {
        FinancialAggregate totalExpensesBySubcategory = budgetService.getTotalExpensesBySubcategory(budgetId, subcategoryId, user);
        return new ResponseEntity<>(FinancialAggregateMapper.toDTO(totalExpensesBySubcategory), HttpStatus.OK);
    }

    @GetMapping("/budgets/{budgetId}/categories/{categoryId}/incomes/total")
    public ResponseEntity<FinancialAggregateDTO> getTotalIncomesForBudgetAndCategory(@PathVariable("budgetId") Long budgetId, @PathVariable("categoryId") Long categoryId, @AuthenticationPrincipal User user) {
        FinancialAggregate totalIncomesByCategory = budgetService.getTotalIncomesByCategory(budgetId, categoryId, user);
        return new ResponseEntity<>(FinancialAggregateMapper.toDTO(totalIncomesByCategory), HttpStatus.OK);
    }

    @GetMapping("/budgets/{budgetId}/subcategories/{subcategoryId}/incomes/total")
    public ResponseEntity<FinancialAggregateDTO> getTotalIncomesForBudgetAndSubcategory(@PathVariable("budgetId") Long budgetId, @PathVariable("subcategoryId") Long subcategoryId, @AuthenticationPrincipal User user) {
        FinancialAggregate totalIncomesBySubcategory = budgetService.getTotalIncomesBySubcategory(budgetId, subcategoryId, user);
        return new ResponseEntity<>(FinancialAggregateMapper.toDTO(totalIncomesBySubcategory), HttpStatus.OK);
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
        ExpensesSummary expensesSummary = budgetService.calculateExpensesSummary(user, budgetId);
        return new ResponseEntity<>(ExpensesSummaryMapper.toDTO(expensesSummary), HttpStatus.OK);
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