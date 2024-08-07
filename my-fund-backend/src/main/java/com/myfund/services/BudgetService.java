package com.myfund.services;

import com.myfund.exceptions.*;
import com.myfund.models.*;
import com.myfund.models.DTOs.*;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.models.DTOs.mappers.ExpenseMapper;
import com.myfund.models.DTOs.mappers.FinancialAggregateMapper;
import com.myfund.models.DTOs.mappers.IncomeMapper;
import com.myfund.repositories.BudgetRepository;
import com.myfund.repositories.ExpenseRepository;
import com.myfund.repositories.IncomeRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;

    private final ExpenseRepository expenseRepository;

    private final IncomeRepository incomeRepository;

    private final CategoryService categoryService;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository, IncomeRepository incomeRepository, CategoryService categoryService) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.categoryService = categoryService;
    }

    public void createDefaultBudget(User user) {

        Budget budget = Budget.builder()
                .name("Default Budget")
                .user(user)
                .localDateTime(LocalDateTime.now())
                .build();
        budgetRepository.save(budget);
        log.info("Default empty budget saved for user. Email: {}", user.getEmail());
    }

    public Budget createBudget(Budget budget, User user) throws InvalidInputException {
        if (budget == null || budget.getName() == null || budget.getName().isEmpty()) {
            throw new InvalidInputException("Budget name is required");
        }

        Optional<Budget> budgetOpt = budgetRepository.findByNameAndUser(budget.getName(), user);
        if (budgetOpt.isPresent()) {
            String errorMessage = String.format("Attempt to create a duplicate budget. User Email: %s, Budget Name: %s", user.getEmail(), budgetOpt.get().getName());
            log.warn(errorMessage);
            throw new BudgetNotUniqueException(errorMessage);
        }

        budget.setUser(user);
        budget.setLocalDateTime(LocalDateTime.now());
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        budgetRepository.save(budget);

        log.info("New budget saved for user. Email: {}. Name: {}", user.getEmail(), budget.getName());
        return budget;
    }

    public List<Budget> findAllBudgetsByUser(User user) {
        List<Budget> allBudgetsByUser = budgetRepository.findAllByUser(user);
        log.info("Retrieved {} budgets for user with ID: {}", allBudgetsByUser.size(), user.getId());
        return allBudgetsByUser;
    }

    public Budget findBudgetByIdAndUser(Long budgetId, User user) {
        log.debug("Starting to find budget by ID: {} for user ID: {}", budgetId, user.getId());

        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (!budgetOpt.isPresent()) {
            log.warn("Budget not found for user with ID: {} and budget ID: {}", user.getId(), budgetId);
            throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
        }

        log.info("Budget found for user with ID: {} and budget ID: {}", user.getId(), budgetId);
        return budgetOpt.get();
    }

    public Expense createExpense(Long budgetId, Expense expense, User user) throws InvalidInputException {
        log.debug("Starting to create expense for budget ID: {} and user ID: {}", budgetId, user.getId());

        if (expense.getName() == null || expense.getName().isEmpty() || expense.getAmount() == null) {
            log.warn("Expense name is required. User ID: {}, Budget ID: {}", user.getId(), budgetId);
            throw new InvalidInputException("Expense name is required");
        }

        if (expense.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Amount cannot be negative for expense creation. User ID: {}, Budget ID: {}", user.getId(), budgetId);
            throw new InvalidInputException("Amount cannot be negative");
        }

        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (!budgetOpt.isPresent()) {
            log.warn("Budget not found for user ID: {} and budget ID: {}", user.getId(), budgetId);
            throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
        }

        log.debug("Budget found for budget ID: {} and user ID: {}. Proceeding with expense creation.", budgetId, user.getId());

        if (!validateCategoryAndSubCategory(expense.getIdCategory(), expense.getIdSubCategory(), user)) {
            log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}", expense.getIdSubCategory(), expense.getIdCategory(), user.getId());
            throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + expense.getIdSubCategory() + " is not related to category with ID: " + expense.getIdCategory());
        }

        Budget budget = budgetOpt.get();
        expense.setBudget(budget);
        expense.setLocalDateTime(LocalDate.now().atStartOfDay());
        expense.setUser(user);
        Expense savedExpense = expenseRepository.save(expense);
        updateTotalExpense(budget);

        log.info("Expense created for budget ID: {} and user ID: {}", budgetId, user.getId());
        return savedExpense;
    }

    public Income createIncome(Long budgetId, Income income, User user) throws InvalidInputException {
        log.debug("Starting to create income for budget ID: {} and user ID: {}", budgetId, user.getId());

        if (income.getName() == null || income.getName().isEmpty() || income.getAmount() == null) {
            log.warn("Expense name is required. User ID: {}, Budget ID: {}", user.getId(), budgetId);
            throw new InvalidInputException("Expense name is required");
        }

        if (income.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Amount cannot be negative for expense creation. User ID: {}, Budget ID: {}", user.getId(), budgetId);
            throw new InvalidInputException("Amount cannot be negative");
        }

        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (!budgetOpt.isPresent()) {
            log.warn("Budget not found for user ID: {} and budget ID: {}", user.getId(), budgetId);
            throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
        }

        log.debug("Budget found for budget ID: {} and user ID: {}. Proceeding with income creation.", budgetId, user.getId());

        if (!validateCategoryAndSubCategory(income.getIdCategory(), income.getIdSubCategory(), user)) {
            log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}", income.getIdSubCategory(), income.getIdCategory(), user.getId());
            throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + income.getIdSubCategory() + " is not related to category with ID: " + income.getIdCategory());
        }

        Budget budget = budgetOpt.get();
        income.setBudget(budget);
        income.setLocalDateTime(LocalDate.now().atStartOfDay());
        income.setUser(user);
        Income savedIncome = incomeRepository.save(income);
        updateTotalIncome(budget);

        log.info("Income created for budget ID: {} and user ID: {}", budgetId, user.getId());
        return savedIncome;
    }

    public Expense updateExpense(Long budgetId, Long expenseId, Expense expense, User user) throws InvalidInputException {
        log.debug("Starting to update expense. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());

        if (expense.getName() == null || expense.getName().isEmpty() || expense.getAmount() == null) {
            log.warn("Expense name is required. User ID: {}, Budget ID: {}", user.getId(), budgetId);
            throw new InvalidInputException("Expense name is required");
        }

        if (expense.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Amount cannot be negative for expense creation. User ID: {}, Budget ID: {}", user.getId(), budgetId);
            throw new InvalidInputException("Amount cannot be negative");
        }

        Optional<Expense> expenseOpt = expenseRepository.findByIdAndUserIdAndBudgetId(expenseId, user.getId(), budgetId);
        if (!expenseOpt.isPresent()) {
            log.warn("Expense not found for update. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
            throw new ExpenseNotFoundException("Expense not found for user with ID: " + user.getId() + ", budget ID: " + budgetId + " and expense ID: " + expenseId);
        }

        log.debug("Expense found for update. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
        Expense existingExpense = expenseOpt.get();

        if (!validateCategoryAndSubCategory(expense.getIdCategory(), expense.getIdSubCategory(), user)) {
            log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}. Update failed.", expense.getIdSubCategory(), expense.getIdCategory(), user.getId());
            throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + expense.getIdSubCategory() + " is not related to category with ID: " + expense.getIdCategory());
        }

        existingExpense.setIdCategory(expense.getIdCategory());
        existingExpense.setIdSubCategory(expense.getIdSubCategory());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setName(expense.getName());
        existingExpense.setLocalDateTime(LocalDate.now().atStartOfDay());
        Expense savedExpense = expenseRepository.save(existingExpense);

        log.info("Expense successfully updated. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
        return savedExpense;
    }

    public Income updateIncome(Long budgetId, Long incomeId, Income income, User user) throws InvalidInputException {
        log.debug("Starting to update income. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());

        if (income.getName() == null || income.getName().isEmpty() || income.getAmount() == null) {
            log.warn("Expense name is required. User ID: {}, Budget ID: {}", user.getId(), budgetId);
            throw new InvalidInputException("Expense name is required");
        }

        if (income.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Amount cannot be negative for expense creation. User ID: {}, Budget ID: {}", user.getId(), budgetId);
            throw new InvalidInputException("Amount cannot be negative");
        }

        Optional<Income> incomeOpt = incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId);
        if (!incomeOpt.isPresent()) {
            log.warn("Income not found for update. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
            throw new IncomeNotFoundException("Income not found for user with ID: " + user.getId() + ", budget ID: " + budgetId + " and income ID: " + incomeId);
        }

        log.debug("Income found for update. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
        Income existingIncome = incomeOpt.get();

        if (!validateCategoryAndSubCategory(income.getIdCategory(), income.getIdSubCategory(), user)) {
            log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}. Update failed.", income.getIdSubCategory(), income.getIdCategory(), user.getId());
            throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + income.getIdSubCategory() + " is not related to category with ID: " + income.getIdCategory());
        }

        existingIncome.setIdCategory(income.getIdCategory());
        existingIncome.setIdSubCategory(income.getIdSubCategory());
        existingIncome.setAmount(income.getAmount());
        existingIncome.setName(income.getName());
        existingIncome.setLocalDateTime(LocalDate.now().atStartOfDay());
        Income savedIncome = incomeRepository.save(existingIncome);

        log.info("Income successfully updated. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
        return savedIncome;
    }

    public boolean validateCategoryAndSubCategory(Long categoryId, Long subCategoryId, User user) {
        log.debug("Validating category and subcategory for user ID: {}, Category ID: {}, Subcategory ID: {}", user.getId(), categoryId, subCategoryId);
        if (categoryId == null && subCategoryId == null) {
            log.debug("Both category and subcategory IDs are null for user ID: {}. Validation passed.", user.getId());
            return true;
        }
        if (categoryId != null && subCategoryId == null) {
            log.debug("Category ID is provided without a subcategory ID for user ID: {}. Validation passed.", user.getId());
            return true;
        }
        if (categoryId == null) {
            log.warn("Subcategory ID: {} cannot be assigned without a parent category for user ID: {}", subCategoryId, user.getId());
            throw new SubcategoryNotFoundException("Subcategory with ID: " + subCategoryId + " cannot be assigned without a parent category for user with ID: " + user.getId());
        }
        return categoryService.isSubcategoryRelatedToCategory(subCategoryId, categoryId, user);
    }

    private void updateTotalExpense(Budget budget) {
        log.debug("Starting to update total expense for budget ID: {}", budget.getId());
        List<Expense> expenses = expenseRepository.findByBudget(budget);
        BigDecimal totalExpense = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        budget.setTotalExpense(totalExpense);
        updateBalance(budget);
        budgetRepository.save(budget);
        log.info("Total expense updated for budget ID: {}. New total expense: {}", budget.getId(), totalExpense);
    }

    private void updateBalance(Budget budget) {
        log.debug("Starting to update balance for budget ID: {}", budget.getId());
        budget.setBalance(budget.getTotalIncome().subtract(budget.getTotalExpense()));
        budgetRepository.save(budget);
        log.info("Balance updated for budget ID: {}. New balance: {}", budget.getId(), budget.getBalance());
    }

    private void updateTotalIncome(Budget budget) {
        log.debug("Starting to update total income for budget ID: {}", budget.getId());
        List<Income> incomes = incomeRepository.findByBudget(budget);
        BigDecimal totalIncome = incomes.stream().map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        budget.setTotalIncome(totalIncome);
        updateBalance(budget);
        budgetRepository.save(budget);
        log.info("Total income updated for budget ID: {}. New total income: {}", budget.getId(), totalIncome);
    }

    public void updateExpensesCategoryIdToNull(Long idCategory) {
        log.debug("Starting to update category ID to null for all expenses with category ID: {}", idCategory);
        List<Expense> expenses = expenseRepository.findByIdCategory(idCategory);
        for (Expense expense : expenses) {
            expense.setIdCategory(null);
            expense.setIdSubCategory(null);
            expenseRepository.save(expense);
        }
        log.info("All expenses with category ID: {} have been updated to have null category and subcategory IDs", idCategory);
    }

    public void updateIncomesCategoryIdToNull(Long idCategory) {
        log.debug("Starting to update category ID to null for all incomes with category ID: {}", idCategory);
        List<Income> incomes = incomeRepository.findByIdCategory(idCategory);
        for (Income income : incomes) {
            income.setIdCategory(null);
            income.setIdSubCategory(null);
            incomeRepository.save(income);
        }
        log.info("All incomes with category ID: {} have been updated to have null category and subcategory IDs", idCategory);
    }

    public void updateExpensesSubcategoryIdToNull(Long subcategoryId) {
        log.debug("Starting to update subcategory ID to null for all expenses with subcategory ID: {}", subcategoryId);
        List<Expense> expenses = expenseRepository.findByIdSubCategory(subcategoryId);
        for (Expense expense : expenses) {
            expense.setIdSubCategory(null);
            expenseRepository.save(expense);
        }
        log.info("All expenses with subcategory ID: {} have been updated to have null subcategory ID", subcategoryId);
    }

    public void updateIncomesSubcategoryIdToNull(Long subcategoryId) {
        log.debug("Starting to update subcategory ID to null for all incomes with subcategory ID: {}", subcategoryId);
        List<Income> incomes = incomeRepository.findByIdSubCategory(subcategoryId);
        for (Income income : incomes) {
            income.setIdSubCategory(null);
            incomeRepository.save(income);
        }
        log.info("All incomes with subcategory ID: {} have been updated to have null subcategory ID", subcategoryId);
    }

    public FinancialAggregate getTotalExpensesByCategory(Long budgetId, Long categoryId, User user) {
        log.debug("Starting to get total expenses for budget ID: {}, category ID: {}, and user ID: {}", budgetId, categoryId, user.getId());
        try {
            List<Expense> expenses = expenseRepository.findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId());
            BigDecimal totalExpenses = expenses != null ? expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
            FinancialAggregate financialAggregate = FinancialAggregate.builder()
                    .budgetId(budgetId)
                    .categoryId(categoryId)
                    .typeAggregate(TypeAggregate.EXPENSES_BY_CATEGORY)
                    .userId(user.getId())
                    .value(totalExpenses)
                    .build();
            log.info("Total expenses retrieved for budget ID: {}, category ID: {}, and user ID: {}. Total: {}", budgetId, categoryId, user.getId(), totalExpenses);
            return financialAggregate;
        } catch (Exception e) {
            log.error("Error retrieving total expenses for budget ID: {}, category ID: {}, and user ID: {}. Error: {}", budgetId, categoryId, user.getId(), e.getMessage());
            throw new TotalExpensesRetrievalException("Error retrieving total expenses for budget ID: " + budgetId + ", category ID: " + categoryId + ", and user ID: " + user.getId());
        }
    }

    public FinancialAggregate getTotalExpensesBySubcategory(Long budgetId, Long subcategoryId, User user) {
        log.debug("Starting to get total expenses for budget ID: {}, subcategory ID: {}, and user ID: {}", budgetId, subcategoryId, user.getId());
        try {
            List<Expense> expenses = expenseRepository.findByIdSubCategoryAndUserIdAndBudgetId(budgetId, subcategoryId, user.getId());
            BigDecimal totalExpenses = expenses != null ? expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
            FinancialAggregate financialAggregate = FinancialAggregate.builder()
                    .budgetId(budgetId)
                    .subcategoryId(subcategoryId)
                    .typeAggregate(TypeAggregate.EXPENSES_BY_SUBCATEGORY)
                    .userId(user.getId())
                    .value(totalExpenses)
                    .build();
            if (totalExpenses == null) {
                log.info("No expenses found for budget ID: {}, subcategory ID: {}, and user ID: {}. Returning ZERO.", budgetId, subcategoryId, user.getId());
                financialAggregate.setValue(BigDecimal.ZERO);
                return financialAggregate;
            }
            financialAggregate.setValue(totalExpenses);
            log.info("Total expenses retrieved for budget ID: {}, subcategory ID: {}, and user ID: {}. Total: {}", budgetId, subcategoryId, user.getId(), totalExpenses);
            return financialAggregate;
        } catch (Exception e) {
            log.error("Error retrieving total expenses for budget ID: {}, subcategory ID: {}, and user ID: {}. Error: {}", budgetId, subcategoryId, user.getId(), e.getMessage());
            throw new TotalExpensesRetrievalException("Error retrieving total expenses for budget ID: " + budgetId + ", subcategory ID: " + subcategoryId + ", and user ID: " + user.getId());
        }
    }

    public FinancialAggregate getTotalIncomesByCategory(Long budgetId, Long categoryId, User user) {
        log.debug("Starting to get total incomes for budget ID: {}, category ID: {}, and user ID: {}", budgetId, categoryId, user.getId());
        try {
            List<Income> incomes = incomeRepository.findByIdCategoryAndUserIdAndBudgetId(budgetId, categoryId, user.getId());
            BigDecimal totalIncomes = incomes != null ? incomes.stream()
                    .map(Income::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
            FinancialAggregate financialAggregate = FinancialAggregate.builder()
                    .budgetId(budgetId)
                    .categoryId(categoryId)
                    .typeAggregate(TypeAggregate.INCOMES_BY_CATEGORY)
                    .userId(user.getId())
                    .value(totalIncomes)
                    .build();
            if (totalIncomes == null) {
                log.info("No incomes found for budget ID: {}, category ID: {}, and user ID: {}. Returning ZERO.", budgetId, categoryId, user.getId());
                financialAggregate.setValue(BigDecimal.ZERO);
                return financialAggregate;
            }
            financialAggregate.setValue(totalIncomes);
            log.info("Total incomes retrieved for budget ID: {}, category ID: {}, and user ID: {}. Total: {}", budgetId, categoryId, user.getId(), totalIncomes);
            return financialAggregate;
        } catch (Exception e) {
            log.error("Error retrieving total incomes for budget ID: {}, category ID: {}, and user ID: {}. Error: {}", budgetId, categoryId, user.getId(), e.getMessage());
            throw new TotalIncomesRetrievalException("Error retrieving total incomes for budget ID: " + budgetId + ", category ID: " + categoryId + ", and user ID: " + user.getId());
        }
    }

    public FinancialAggregate getTotalIncomesBySubcategory(Long budgetId, Long subcategoryId, User user) {
        log.debug("Starting to get total incomes for budget ID: {}, subcategory ID: {}, and user ID: {}", budgetId, subcategoryId, user.getId());
        try {
            List<Income> incomes = incomeRepository.findByIdSubCategoryAndUserIdAndBudgetId(budgetId, subcategoryId, user.getId());
            BigDecimal totalIncomes = incomes != null ? incomes.stream()
                    .map(Income::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
            FinancialAggregate financialAggregate = FinancialAggregate.builder()
                    .budgetId(budgetId)
                    .subcategoryId(subcategoryId)
                    .typeAggregate(TypeAggregate.INCOMES_BY_SUBCATEGORY)
                    .userId(user.getId())
                    .build();
            if (totalIncomes == null) {
                log.info("No incomes found for budget ID: {}, subcategory ID: {}, and user ID: {}. Returning ZERO.", budgetId, subcategoryId, user.getId());
                financialAggregate.setValue(BigDecimal.ZERO);
                return financialAggregate;
            }
            financialAggregate.setValue(totalIncomes);
            log.info("Total incomes retrieved for budget ID: {}, subcategory ID: {}, and user ID: {}. Total: {}", budgetId, subcategoryId, user.getId(), totalIncomes);
            return financialAggregate;
        } catch (Exception e) {
            log.error("Error retrieving total incomes for budget ID: {}, subcategory ID: {}, and user ID: {}. Error: {}", budgetId, subcategoryId, user.getId(), e.getMessage());
            throw new TotalIncomesRetrievalException("Error retrieving total incomes for budget ID: " + budgetId + ", subcategory ID: " + subcategoryId + ", and user ID: " + user.getId());
        }
    }

    @Transactional
    public void deleteBudgetByIdAndUser(Long budgetId, User user) {
        log.debug("Starting to delete budget ID: {} and user ID: {}", budgetId, user.getId());
        if (!budgetRepository.existsByIdAndUserId(budgetId, user.getId())) {
            log.error("Budget ID: {} for user ID: {} not found.", budgetId, user.getId());
            throw new BudgetNotFoundException("Budget ID: " + budgetId + " and user ID: " + user.getId() + " not found.");
        }
        try {
            deleteExpensesForBudget(budgetId);
            deleteIncomesForBudget(budgetId);
            budgetRepository.deleteBudgetByIdAndUser(budgetId, user);
            log.info("Budget ID: {} for user ID: {} successfully deleted.", budgetId, user.getId());
        } catch (Exception e) {
            log.error("Error deleting budget ID: {} for user ID: {}. Error: {}", budgetId, user.getId(), e.getMessage());
            throw new BudgetNotFoundException("Error deleting budget ID: " + budgetId + " and user ID: " + user.getId());
        }
    }

    @Transactional
    private void deleteExpensesForBudget(Long budgetId) {
        List<Expense> expenses = expenseRepository.findByBudgetId(budgetId);
        if (!expenses.isEmpty()) {
            expenseRepository.deleteAll(expenses);
            log.info("All expenses for budget ID: {} have been deleted. Deleted expenses: {}", budgetId, expenses.size());
        }
    }

    @Transactional
    private void deleteIncomesForBudget(Long budgetId) {
        List<Income> incomes = incomeRepository.findByBudgetId(budgetId);
        if (!incomes.isEmpty()) {
            incomeRepository.deleteAll(incomes);
            log.info("All incomes for budget ID: {} have been deleted. Deleted incomes: {}", budgetId, incomes.size());
        }
    }

    @Transactional
    public void deleteExpenseByIdAndUser(Long expenseId, User user, Long budgetId) {
        log.debug("Starting to delete expense ID: {} and user ID: {}", expenseId, user.getId());
        try {
            Optional<Expense> expenseOpt = expenseRepository.findByIdAndUserIdAndBudgetId(expenseId, user.getId(), budgetId);
            if (expenseOpt.isEmpty()) {
                log.error("Expense ID: {} is not associated with budget ID: {} for user ID: {}", expenseId, budgetId, user.getId());
                throw new ExpenseNotFoundException("Expense ID: " + expenseId + " is not associated with budget ID: " + budgetId + " for user ID: " + user.getId());
            }
            expenseRepository.deleteExpenseByIdAndUserAndBudgetId(expenseId, user, budgetId);
            log.info("Expense ID: {} for user ID: {} successfully deleted.", expenseId, user.getId());
        } catch (Exception e) {
            log.error("Error deleting expense ID: {} for user ID: {}. Error: {}", expenseId, user.getId(), e.getMessage());
            throw new ExpenseNotFoundException(e.getMessage());
        }
    }

    @Transactional
    public void deleteIncomeByIdAndUser(Long incomeId, User user, Long budgetId) {
        log.debug("Starting to delete income ID: {} and user ID: {}", incomeId, user.getId());
        try {
            Optional<Income> incomeOpt = incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId);
            if (incomeOpt.isEmpty()) {
                log.error("Income ID: {} is not associated with budget ID: {} for user ID: {}", incomeId, budgetId, user.getId());
                throw new IncomeNotFoundException("Income ID: " + incomeId + " is not associated with budget ID: " + budgetId + " for user ID: " + user.getId());
            }
            incomeRepository.deleteExpenseByIdAndUserAndBudgetId(incomeId, user, budgetId);
            log.info("Income ID: {} for user ID: {} successfully deleted.", incomeId, user.getId());
        } catch (Exception e) {
            log.error("Error deleting income ID: {} for user ID: {}. Error: {}", incomeId, user.getId(), e.getMessage());
            throw new IncomeNotFoundException(e.getMessage());
        }
    }

    public ExpensesSummary calculateExpensesSummary(User user, Long budgetId) {

        log.debug("Calculating expenses summary for user ID: {} and budget ID: {}", user.getId(), budgetId);

        List<Expense> expenses = expenseRepository.findByBudgetIdAndUser(budgetId, user);

        Map<Long, ExpensesSummary.CategoryExpenses> categoryMap = new HashMap<>();
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount());
            Long categoryId = expense.getIdCategory();
            ExpensesSummary.CategoryExpenses categoryExpenses = categoryMap.computeIfAbsent(categoryId, k -> {
                ExpensesSummary.CategoryExpenses newCategoryExpenses = new ExpensesSummary.CategoryExpenses();
                newCategoryExpenses.setCategoryId(categoryId);
                return newCategoryExpenses;
            });
            categoryExpenses.setTotalExpenses(categoryExpenses.getTotalExpenses().add(expense.getAmount()));
            Long subcategoryId = expense.getIdSubCategory();
            ExpensesSummary.CategoryExpenses.SubcategoryExpenses subcategoryExpenses = new ExpensesSummary.CategoryExpenses.SubcategoryExpenses();
            subcategoryExpenses.setSubcategoryId(subcategoryId);
            subcategoryExpenses.setExpenseAmount(expense.getAmount());
            categoryExpenses.getSubcategories().add(subcategoryExpenses);
        }

        ExpensesSummary expensesSummary = new ExpensesSummary();
        for (ExpensesSummary.CategoryExpenses categoryExpenses : categoryMap.values()) {
            categoryExpenses.setPercentageOfTotal(categoryExpenses.getTotalExpenses().multiply(BigDecimal.valueOf(100)).divide(totalExpenses, 2, RoundingMode.HALF_UP));
            expensesSummary.getExpensesSummary().add(categoryExpenses);
        }

        log.debug("Expenses summary calculated for user ID: {} and budget ID: {}. Total expenses: {}", user.getId(), budgetId, totalExpenses);

        return expensesSummary;
    }

    public void saveExpenseFromCsv(Expense expense) {
        log.debug("Starting to save expense from CSV");
        expenseRepository.save(expense);
        log.debug("Expense from CSV successfully saved");
    }

    public void saveIncomeFromCsv(Income income) {
        log.debug("Starting to save income from CSV");
        incomeRepository.save(income);
        log.debug("Income from CSV successfully saved");
    }
}




