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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
        Budget budget = new Budget();
        budget.setName("Default Budget");
        budget.setUser(user);
        budget.setLocalDateTime(LocalDateTime.now());
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        budgetRepository.save(budget);
        log.info("Default empty budget saved for user. Email: {}", user.getEmail());
    }

    public BudgetDTO createBudget(CreateBudgetDTO createBudgetDTO, User user) {
        Optional<Budget> budgetOpt = budgetRepository.findByNameAndUser(createBudgetDTO.getName(), user);
        if (budgetOpt.isPresent()) {
            String errorMessage = String.format("Attempt to create a duplicate budget. User Email: %s, Budget Name: %s", user.getEmail(), budgetOpt.get().getName());
            log.warn(errorMessage);
            throw new BudgetNotUniqueException(errorMessage);
        }

        Budget budget = BudgetMapper.createBudgetDTOMapToBudget(createBudgetDTO);
        budget.setUser(user);
        budget.setLocalDateTime(LocalDateTime.now());
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        budgetRepository.save(budget);

        log.info("New budget saved for user. Email: {}. Name: {}", user.getEmail(), budget.getName());
        return BudgetMapper.budgetMapToBudgetDTO(budget);
    }

    public List<BudgetSummaryDTO> findAllBudgetsByUser(User user) {
        List<BudgetSummaryDTO> budgetSummaryDTOList = BudgetMapper.budgetListMapToBudgetSummaryDTOList(budgetRepository.findAllByUser(user));
        log.info("Retrieved {} budgets for user with ID: {}", budgetSummaryDTOList.size(), user.getId());
        return budgetSummaryDTOList;
    }

    public BudgetDTO findBudgetByIdAndUser(Long budgetId, User user) {
        log.debug("Starting to find budget by ID: {} for user ID: {}", budgetId, user.getId());

        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (!budgetOpt.isPresent()) {
            log.warn("Budget not found for user with ID: {} and budget ID: {}", user.getId(), budgetId);
            throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
        }

        log.info("Budget found for user with ID: {} and budget ID: {}", user.getId(), budgetId);
        return BudgetMapper.budgetMapToBudgetDTO(budgetOpt.get());
    }

    public ExpenseDTO createExpense(Long budgetId, CreateExpenseDTO createExpenseDTO, User user) {
        log.debug("Starting to create expense for budget ID: {} and user ID: {}", budgetId, user.getId());

        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (!budgetOpt.isPresent()) {
            log.warn("Budget not found for user ID: {} and budget ID: {}", user.getId(), budgetId);
            throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
        }

        log.debug("Budget found for budget ID: {} and user ID: {}. Proceeding with expense creation.", budgetId, user.getId());

        if (!validateCategoryAndSubCategory(createExpenseDTO.getIdCategory(), createExpenseDTO.getIdSubCategory(), user)) {
            log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}", createExpenseDTO.getIdSubCategory(), createExpenseDTO.getIdCategory(), user.getId());
            throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createExpenseDTO.getIdSubCategory() + " is not related to category with ID: " + createExpenseDTO.getIdCategory());
        }

        Expense expense = ExpenseMapper.createExpenseDTOtoExpense(createExpenseDTO);
        Budget budget = budgetOpt.get();
        expense.setBudget(budget);
        expense.setLocalDateTime(LocalDate.now().atStartOfDay());
        expense.setUser(user);
        expenseRepository.save(expense);
        updateTotalExpense(budget);

        log.info("Expense created for budget ID: {} and user ID: {}", budgetId, user.getId());
        return ExpenseMapper.expensetoExpenseDTO(expense);
    }

    public IncomeDTO createIncome(Long budgetId, CreateIncomeDTO createIncomeDTO, User user) {
        log.debug("Starting to create income for budget ID: {} and user ID: {}", budgetId, user.getId());

        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (!budgetOpt.isPresent()) {
            log.warn("Budget not found for user ID: {} and budget ID: {}", user.getId(), budgetId);
            throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
        }

        log.debug("Budget found for budget ID: {} and user ID: {}. Proceeding with income creation.", budgetId, user.getId());

        if (!validateCategoryAndSubCategory(createIncomeDTO.getIdCategory(), createIncomeDTO.getIdSubCategory(), user)) {
            log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}", createIncomeDTO.getIdSubCategory(), createIncomeDTO.getIdCategory(), user.getId());
            throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createIncomeDTO.getIdSubCategory() + " is not related to category with ID: " + createIncomeDTO.getIdCategory());
        }

        Income income = IncomeMapper.createIncomeDTOtoIncome(createIncomeDTO);
        Budget budget = budgetOpt.get();
        income.setBudget(budget);
        income.setLocalDateTime(LocalDate.now().atStartOfDay());
        income.setUser(user);
        incomeRepository.save(income);
        updateTotalIncome(budget);

        log.info("Income created for budget ID: {} and user ID: {}", budgetId, user.getId());
        return IncomeMapper.incomeMapToIncomeDTO(income);
    }

    public ExpenseDTO updateExpense(Long budgetId, Long expenseId, CreateExpenseDTO createExpenseDTO, User user) {
        log.debug("Starting to update expense. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());

        Optional<Expense> expenseOpt = expenseRepository.findByIdAndUserIdAndBudgetId(expenseId, user.getId(), budgetId);
        if (!expenseOpt.isPresent()) {
            log.warn("Expense not found for update. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
            throw new ExpenseNotFoundException("Expense not found for user with ID: " + user.getId() + ", budget ID: " + budgetId + " and expense ID: " + expenseId);
        }

        log.debug("Expense found for update. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
        Expense expense = expenseOpt.get();

        if (!validateCategoryAndSubCategory(createExpenseDTO.getIdCategory(), createExpenseDTO.getIdSubCategory(), user)) {
            log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}. Update failed.", createExpenseDTO.getIdSubCategory(), createExpenseDTO.getIdCategory(), user.getId());
            throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createExpenseDTO.getIdSubCategory() + " is not related to category with ID: " + createExpenseDTO.getIdCategory());
        }

        expense.setIdCategory(createExpenseDTO.getIdCategory());
        expense.setIdSubCategory(createExpenseDTO.getIdSubCategory());
        expense.setAmount(createExpenseDTO.getAmount());
        expense.setName(createExpenseDTO.getName());
        expense.setLocalDateTime(LocalDate.now().atStartOfDay());
        expenseRepository.save(expense);

        log.info("Expense successfully updated. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
        return ExpenseMapper.expensetoExpenseDTO(expense);
    }

    public IncomeDTO updateIncome(Long budgetId, Long incomeId, CreateIncomeDTO createIncomeDTO, User user) {
        log.debug("Starting to update income. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());

        Optional<Income> incomeOpt = incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId);
        if (!incomeOpt.isPresent()) {
            log.warn("Income not found for update. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
            throw new IncomeNotFoundException("Income not found for user with ID: " + user.getId() + ", budget ID: " + budgetId + " and income ID: " + incomeId);
        }

        log.debug("Income found for update. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
        Income income = incomeOpt.get();

        if (!validateCategoryAndSubCategory(createIncomeDTO.getIdCategory(), createIncomeDTO.getIdSubCategory(), user)) {
            log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}. Update failed.", createIncomeDTO.getIdSubCategory(), createIncomeDTO.getIdCategory(), user.getId());
            throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createIncomeDTO.getIdSubCategory() + " is not related to category with ID: " + createIncomeDTO.getIdCategory());
        }

        income.setIdCategory(createIncomeDTO.getIdCategory());
        income.setIdSubCategory(createIncomeDTO.getIdSubCategory());
        income.setAmount(createIncomeDTO.getAmount());
        income.setName(createIncomeDTO.getName());
        income.setLocalDateTime(LocalDate.now().atStartOfDay());
        incomeRepository.save(income);

        log.info("Income successfully updated. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
        return IncomeMapper.incomeMapToIncomeDTO(income);
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

    public FinancialAggregateCategoryDTO getTotalExpensesByCategory(Long budgetId, Long categoryId, User user) {
        log.debug("Starting to get total expenses for budget ID: {}, category ID: {}, and user ID: {}", budgetId, categoryId, user.getId());
        FinancialAggregate financialAggregate = new FinancialAggregate();
        try {
            BigDecimal totalExpenses = expenseRepository.sumExpensesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId());
            financialAggregate.setBudgetId(budgetId);
            financialAggregate.setCategoryId(categoryId);
            financialAggregate.setTypeAggregate(TypeAggregate.EXPENSES_BY_CATEGORY);
            financialAggregate.setUserId(user.getId());
            if (totalExpenses == null) {
                log.info("No expenses found for budget ID: {}, category ID: {}, and user ID: {}. Returning ZERO.", budgetId, categoryId, user.getId());
                financialAggregate.setValue(BigDecimal.ZERO);
                return FinancialAggregateMapper.toFinancialAggregateCategoryDTO(financialAggregate);
            }
            financialAggregate.setValue(totalExpenses);
            log.info("Total expenses retrieved for budget ID: {}, category ID: {}, and user ID: {}. Total: {}", budgetId, categoryId, user.getId(), totalExpenses);
            return FinancialAggregateMapper.toFinancialAggregateCategoryDTO(financialAggregate);
        } catch (Exception e) {
            log.error("Error retrieving total expenses for budget ID: {}, category ID: {}, and user ID: {}. Error: {}", budgetId, categoryId, user.getId(), e.getMessage());
            throw new TotalExpensesRetrievalException("Error retrieving total expenses for budget ID: " + budgetId + ", category ID: " + categoryId + ", and user ID: " + user.getId());
        }
    }

    public FinancialAggregateSubcategoryDTO getTotalExpensesBySubcategory(Long budgetId, Long subcategoryId, User user) {
        log.debug("Starting to get total expenses for budget ID: {}, subcategory ID: {}, and user ID: {}", budgetId, subcategoryId, user.getId());
        FinancialAggregate financialAggregate = new FinancialAggregate();
        try {
            BigDecimal totalExpenses = expenseRepository.sumExpensesByBudgetIdAndSubcategoryIdAndUserId(budgetId, subcategoryId, user.getId());
            financialAggregate.setBudgetId(budgetId);
            financialAggregate.setSubcategoryId(subcategoryId);
            financialAggregate.setTypeAggregate(TypeAggregate.EXPENSES_BY_SUBCATEGORY);
            financialAggregate.setUserId(user.getId());
            if (totalExpenses == null) {
                log.info("No expenses found for budget ID: {}, subcategory ID: {}, and user ID: {}. Returning ZERO.", budgetId, subcategoryId, user.getId());
                financialAggregate.setValue(BigDecimal.ZERO);
                return FinancialAggregateMapper.toFinancialAggregateSubcategoryDTO(financialAggregate);
            }
            financialAggregate.setValue(totalExpenses);
            log.info("Total expenses retrieved for budget ID: {}, subcategory ID: {}, and user ID: {}. Total: {}", budgetId, subcategoryId, user.getId(), totalExpenses);
            return FinancialAggregateMapper.toFinancialAggregateSubcategoryDTO(financialAggregate);
        } catch (Exception e) {
            log.error("Error retrieving total expenses for budget ID: {}, subcategory ID: {}, and user ID: {}. Error: {}", budgetId, subcategoryId, user.getId(), e.getMessage());
            throw new TotalExpensesRetrievalException("Error retrieving total expenses for budget ID: " + budgetId + ", subcategory ID: " + subcategoryId + ", and user ID: " + user.getId());
        }
    }

    public FinancialAggregateCategoryDTO getTotalIncomesByCategory(Long budgetId, Long categoryId, User user) {
        log.debug("Starting to get total incomes for budget ID: {}, category ID: {}, and user ID: {}", budgetId, categoryId, user.getId());
        FinancialAggregate financialAggregate = new FinancialAggregate();
        try {
            BigDecimal totalIncomes = incomeRepository.sumIncomesByBudgetIdAndCategoryIdAndUserId(budgetId, categoryId, user.getId());
            financialAggregate.setBudgetId(budgetId);
            financialAggregate.setCategoryId(categoryId);
            financialAggregate.setTypeAggregate(TypeAggregate.INCOMES_BY_CATEGORY);
            financialAggregate.setUserId(user.getId());
            if (totalIncomes == null) {
                log.info("No incomes found for budget ID: {}, category ID: {}, and user ID: {}. Returning ZERO.", budgetId, categoryId, user.getId());
                financialAggregate.setValue(BigDecimal.ZERO);
                return FinancialAggregateMapper.toFinancialAggregateCategoryDTO(financialAggregate);
            }
            financialAggregate.setValue(totalIncomes);
            log.info("Total incomes retrieved for budget ID: {}, category ID: {}, and user ID: {}. Total: {}", budgetId, categoryId, user.getId(), totalIncomes);
            return FinancialAggregateMapper.toFinancialAggregateCategoryDTO(financialAggregate);
        } catch (Exception e) {
            log.error("Error retrieving total incomes for budget ID: {}, category ID: {}, and user ID: {}. Error: {}", budgetId, categoryId, user.getId(), e.getMessage());
            throw new TotalIncomesRetrievalException("Error retrieving total incomes for budget ID: " + budgetId + ", category ID: " + categoryId + ", and user ID: " + user.getId());
        }
    }

    public FinancialAggregateSubcategoryDTO getTotalIncomesBySubcategory(Long budgetId, Long subcategoryId, User user) {
        log.debug("Starting to get total incomes for budget ID: {}, subcategory ID: {}, and user ID: {}", budgetId, subcategoryId, user.getId());
        FinancialAggregate financialAggregate = new FinancialAggregate();
        try {
            BigDecimal totalIncomes = incomeRepository.sumIncomesByBudgetIdAndSubcategoryIdAndUserId(budgetId, subcategoryId, user.getId());
            financialAggregate.setBudgetId(budgetId);
            financialAggregate.setSubcategoryId(subcategoryId);
            financialAggregate.setTypeAggregate(TypeAggregate.INCOMES_BY_SUBCATEGORY);
            financialAggregate.setUserId(user.getId());
            if (totalIncomes == null) {
                log.info("No incomes found for budget ID: {}, subcategory ID: {}, and user ID: {}. Returning ZERO.", budgetId, subcategoryId, user.getId());
                financialAggregate.setValue(BigDecimal.ZERO);
                return FinancialAggregateMapper.toFinancialAggregateSubcategoryDTO(financialAggregate);
            }
            financialAggregate.setValue(totalIncomes);
            log.info("Total incomes retrieved for budget ID: {}, subcategory ID: {}, and user ID: {}. Total: {}", budgetId, subcategoryId, user.getId(), totalIncomes);
            return FinancialAggregateMapper.toFinancialAggregateSubcategoryDTO(financialAggregate);
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

    public ExpensesSummaryDTO calculateExpensesSummary(User user, Long budgetId) {

        log.debug("Calculating expenses summary for user ID: {} and budget ID: {}", user.getId(), budgetId);

        List<Expense> expenses = expenseRepository.findByBudgetIdAndUser(budgetId, user);

        Map<Long, ExpensesSummaryDTO.CategoryExpenses> categoryMap = new HashMap<>();
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount());
            Long categoryId = expense.getIdCategory();
            ExpensesSummaryDTO.CategoryExpenses categoryExpenses = categoryMap.computeIfAbsent(categoryId, k -> {
                ExpensesSummaryDTO.CategoryExpenses newCategoryExpenses = new ExpensesSummaryDTO.CategoryExpenses();
                newCategoryExpenses.setCategoryId(categoryId);
                return newCategoryExpenses;
            });
            categoryExpenses.setTotalExpenses(categoryExpenses.getTotalExpenses().add(expense.getAmount()));
            Long subcategoryId = expense.getIdSubCategory();
            ExpensesSummaryDTO.CategoryExpenses.SubcategoryExpenses subcategoryExpenses = new ExpensesSummaryDTO.CategoryExpenses.SubcategoryExpenses();
            subcategoryExpenses.setSubcategoryId(subcategoryId);
            subcategoryExpenses.setExpenseAmount(expense.getAmount());
            categoryExpenses.getSubcategories().add(subcategoryExpenses);
        }

        ExpensesSummaryDTO expensesSummaryDTO = new ExpensesSummaryDTO();
        for (ExpensesSummaryDTO.CategoryExpenses categoryExpenses : categoryMap.values()) {
            categoryExpenses.setPercentageOfTotal(categoryExpenses.getTotalExpenses().multiply(BigDecimal.valueOf(100)).divide(totalExpenses, 2, RoundingMode.HALF_UP));
            expensesSummaryDTO.getExpensesSummary().add(categoryExpenses);
        }

        log.debug("Expenses summary calculated for user ID: {} and budget ID: {}. Total expenses: {}", user.getId(), budgetId, totalExpenses);

        return expensesSummaryDTO;
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




