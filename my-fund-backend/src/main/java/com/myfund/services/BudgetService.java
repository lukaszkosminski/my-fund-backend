package com.myfund.services;

import com.myfund.exceptions.*;
import com.myfund.models.Budget;
import com.myfund.models.DTOs.*;
import com.myfund.models.DTOs.mappers.BudgetMapper;
import com.myfund.models.DTOs.mappers.ExpenseMapper;
import com.myfund.models.DTOs.mappers.IncomeMapper;
import com.myfund.models.Expense;
import com.myfund.models.Income;
import com.myfund.models.User;
import com.myfund.repositories.BudgetRepository;
import com.myfund.repositories.ExpenseRepository;
import com.myfund.repositories.IncomeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        } else {
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
    }

    public List<BudgetSummaryDTO> findAllBudgetsByUser(User user) {
        List<BudgetSummaryDTO> budgetSummaryDTOList = BudgetMapper.budgetListMapToBudgetSummaryDTOList(budgetRepository.findAllByUser(user));
        log.info("Retrieved {} budgets for user with ID: {}", budgetSummaryDTOList.size(), user.getId());
        return budgetSummaryDTOList;
    }

    public BudgetDTO findBudgetByIdAndUser(Long budgetId, User user) {
        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (budgetOpt.isPresent()) {
            log.info("Budget found for user with ID: {} and budget ID: {}", user.getId(), budgetId);
            return BudgetMapper.budgetMapToBudgetDTO(budgetOpt.get());
        } else {
            log.warn("Budget not found for user with ID: {} and budget ID: {}", user.getId(), budgetId);
            throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
        }
    }

    public ExpenseDTO createExpense(Long budgetId, CreateExpenseDTO createExpenseDTO, User user) {
        log.debug("Starting to create expense for budget ID: {} and user ID: {}", budgetId, user.getId());
        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (budgetOpt.isPresent()) {
            log.debug("Budget found for budget ID: {} and user ID: {}. Proceeding with expense creation.", budgetId, user.getId());
            if (!validateCategoryAndSubCategory(createExpenseDTO.getIdCategory(), createExpenseDTO.getIdSubCategory(), user)) {
                log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}", createExpenseDTO.getIdSubCategory(), createExpenseDTO.getIdCategory(), user.getId());
                throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createExpenseDTO.getIdSubCategory() + " is not related to category with ID: " + createExpenseDTO.getIdCategory());
            }
            Expense expense = ExpenseMapper.createExpenseDTOtoExpense(createExpenseDTO);
            expense.setBudget(budgetOpt.get());
            expense.setLocalDateTime(LocalDateTime.now());
            expense.setUser(user);
            expenseRepository.save(expense);
            updateTotalExpense(budgetOpt.get());
            log.info("Expense created for budget ID: {} and user ID: {}", budgetId, user.getId());
            return ExpenseMapper.expensetoExpenseDTO(expense);
        }
        log.warn("Budget not found for user ID: {} and budget ID: {}", user.getId(), budgetId);
        throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
    }

    public IncomeDTO createIncome(Long budgetId, CreateIncomeDTO createIncomeDTO, User user) {
        log.debug("Starting to create income for budget ID: {} and user ID: {}", budgetId, user.getId());
        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (budgetOpt.isPresent()) {
            log.debug("Budget found for budget ID: {} and user ID: {}. Proceeding with income creation.", budgetId, user.getId());
            if (!validateCategoryAndSubCategory(createIncomeDTO.getIdCategory(), createIncomeDTO.getIdSubCategory(), user)) {
                log.warn("Subcategory with ID: {} is not related to category with ID: {} for user ID: {}", createIncomeDTO.getIdSubCategory(), createIncomeDTO.getIdCategory(), user.getId());
                throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createIncomeDTO.getIdSubCategory() + " is not related to category with ID: " + createIncomeDTO.getIdCategory());
            }
            Income income = IncomeMapper.createIncomeDTOtoIncome(createIncomeDTO);
            income.setBudget(budgetOpt.get());
            income.setLocalDateTime(LocalDateTime.now());
            income.setUser(user);
            incomeRepository.save(income);
            updateTotalIncome(budgetOpt.get());
            log.info("Income created for budget ID: {} and user ID: {}", budgetId, user.getId());
            return IncomeMapper.incomeMapToIncomeDTO(income);
        }
        log.warn("Budget not found for user ID: {} and budget ID: {}", user.getId(), budgetId);
        throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
    }

    public ExpenseDTO updateExpense(Long budgetId, Long expenseId, CreateExpenseDTO createExpenseDTO, User user) {
        log.debug("Starting to update expense. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
        Optional<Expense> expenseOpt = expenseRepository.findByIdAndUserIdAndBudgetId(expenseId, user.getId(), budgetId);
        if (expenseOpt.isPresent()) {
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
            expense.setLocalDateTime(LocalDateTime.now());
            expenseRepository.save(expense);
            log.info("Expense successfully updated. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
            return ExpenseMapper.expensetoExpenseDTO(expense);
        }
        log.warn("Expense not found for update. Expense ID: {}, Budget ID: {}, User ID: {}", expenseId, budgetId, user.getId());
        throw new ExpenseNotFoundException("Expense not found for user with ID: " + user.getId() + ", budget ID: " + budgetId + " and expense ID: " + expenseId);
    }

    public IncomeDTO updateIncome(Long budgetId, Long incomeId, CreateIncomeDTO createIncomeDTO, User user) {
        log.debug("Starting to update income. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
        Optional<Income> incomeOpt = incomeRepository.findByIdAndUserIdAndBudgetId(incomeId, user.getId(), budgetId);
        if (incomeOpt.isPresent()) {
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
            income.setLocalDateTime(LocalDateTime.now());
            incomeRepository.save(income);
            log.info("Income successfully updated. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
            return IncomeMapper.incomeMapToIncomeDTO(income);
        }
        log.warn("Income not found for update. Income ID: {}, Budget ID: {}, User ID: {}", incomeId, budgetId, user.getId());
        throw new IncomeNotFoundException("Income not found for user with ID: " + user.getId() + ", budget ID: " + budgetId + " and expense ID: " + incomeId);
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
        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
}




