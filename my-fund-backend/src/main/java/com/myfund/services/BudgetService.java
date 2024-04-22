package com.myfund.services;

import com.myfund.exceptions.BudgetNotFoundException;
import com.myfund.exceptions.BudgetNotUniqueException;
import com.myfund.exceptions.SubcategoryNotRelatedToCategoryException;
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

    public Optional<BudgetDTO> createBudget(CreateBudgetDTO createBudgetDTO, User user) {
        Optional<Budget> budgetOpt = budgetRepository.findByNameAndUser(createBudgetDTO.getName(), user);
        if (budgetOpt.isPresent()) {
            throw new BudgetNotUniqueException("budget is not unique");
        } else {
            Budget budget = BudgetMapper.createBudgetDTOMapToBudget(createBudgetDTO);
            budget.setUser(user);
            budget.setLocalDateTime(LocalDateTime.now());
            budget.setBalance(BigDecimal.ZERO);
            budget.setTotalExpense(BigDecimal.ZERO);
            budget.setTotalIncome(BigDecimal.ZERO);
            budgetRepository.save(budget);
            log.info("New budget saved for user. Email: {}. Name: {}", user.getEmail(), budget.getName());
            return Optional.of(BudgetMapper.budgetMapToBudgetDTO(budget));
        }
    }

    public List<BudgetSummaryDTO> findAllBudgetsByUser(User user) {
        return BudgetMapper.budgetListMapToBudgetSummaryDTOList(budgetRepository.findAllByUser(user));
    }

    public Optional<BudgetDTO> findBudgetByIdAndUser(Long budgetId, User user) {
        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (budgetOpt.isPresent()) {
            return Optional.of(BudgetMapper.budgetMapToBudgetDTO(budgetOpt.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<ExpenseDTO> createExpense(Long budgetId, CreateExpenseDTO createExpenseDTO, User user) {
        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (budgetOpt.isPresent()) {
            if (!validateCategoryAndSubCategory(createExpenseDTO.getIdCategory(), createExpenseDTO.getIdSubCategory(), user)) {
                throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createExpenseDTO.getIdSubCategory() + " is not related to category with ID: " + createExpenseDTO.getIdCategory());
            }
            Expense expense = ExpenseMapper.createExpenseDTOtoExpense(createExpenseDTO);
            expense.setBudget(budgetOpt.get());
            expense.setLocalDateTime(LocalDateTime.now());
            expense.setUser(user);
            expenseRepository.save(expense);
            updateTotalExpense(budgetOpt.get());
            return Optional.of(ExpenseMapper.expensetoExpenseDTO(expense));
        }
        throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
    }

    public Optional<IncomeDTO> createIncome(Long budgetId, CreateIncomeDTO createIncomeDTO, User user) {
        Optional<Budget> budgetOpt = budgetRepository.findByIdAndUser(budgetId, user);
        if (budgetOpt.isPresent()) {
            if (!validateCategoryAndSubCategory(createIncomeDTO.getIdCategory(), createIncomeDTO.getIdSubCategory(), user)) {
                throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createIncomeDTO.getIdSubCategory() + " is not related to category with ID: " + createIncomeDTO.getIdCategory());
            }
            Income income = IncomeMapper.createIncomeDTOtoIncome(createIncomeDTO);
            income.setBudget(budgetOpt.get());
            income.setLocalDateTime(LocalDateTime.now());
            income.setUser(user);
            incomeRepository.save(income);
            updateTotalIncome(budgetOpt.get());
            return Optional.of(IncomeMapper.incomeMapToIncomeDTO(income));
        }
        throw new BudgetNotFoundException("Budget not found for user with ID: " + user.getId() + " and budget ID: " + budgetId);
    }

    public Optional<ExpenseDTO> updateExpense(Long expenseId, CreateExpenseDTO createExpenseDTO, User user) {
        Optional<Expense> expenseOpt = expenseRepository.findByIdAndUser(expenseId, user);
        if (expenseOpt.isPresent()) {
            Expense expense = expenseOpt.get();
            if (!validateCategoryAndSubCategory(createExpenseDTO.getIdCategory(), createExpenseDTO.getIdSubCategory(), user)) {
                throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createExpenseDTO.getIdSubCategory() + " is not related to category with ID: " + createExpenseDTO.getIdCategory());
            }
            expense.setIdCategory(createExpenseDTO.getIdCategory());
            expense.setIdSubCategory(createExpenseDTO.getIdSubCategory());
            expense.setAmount(createExpenseDTO.getAmount());
            expense.setName(createExpenseDTO.getName());
            expense.setLocalDateTime(LocalDateTime.now());
            expenseRepository.save(expense);
            return Optional.of(ExpenseMapper.expensetoExpenseDTO(expense));
        }
        return Optional.empty();
    }

    public Optional<IncomeDTO> updateIncome(Long incomeId, CreateIncomeDTO createIncomeDTO, User user) {
        Optional<Income> incomeOpt = incomeRepository.findByIdAndUser(incomeId, user);
        if (incomeOpt.isPresent()) {
            Income income = incomeOpt.get();
            if (!validateCategoryAndSubCategory(createIncomeDTO.getIdCategory(), createIncomeDTO.getIdSubCategory(), user)) {
                throw new SubcategoryNotRelatedToCategoryException("Subcategory with ID: " + createIncomeDTO.getIdSubCategory() + " is not related to category with ID: " + createIncomeDTO.getIdCategory());
            }
            income.setIdCategory(createIncomeDTO.getIdCategory());
            income.setIdSubCategory(createIncomeDTO.getIdSubCategory());
            income.setAmount(createIncomeDTO.getAmount());
            income.setName(createIncomeDTO.getName());
            income.setLocalDateTime(LocalDateTime.now());
            incomeRepository.save(income);
            return Optional.of(IncomeMapper.incomeMapToIncomeDTO(income));
        }
        return Optional.empty();
    }

    public boolean validateCategoryAndSubCategory(Long categoryId, Long subCategoryId, User user) {
        if (categoryId == null & subCategoryId == null) {
            return true;
        } else if (categoryService.isSubcategoryRelatedToCategory(subCategoryId, categoryId, user)) {
            return true;
        } else {
            return false;
        }
    }

    private void updateTotalExpense(Budget budget) {
        List<Expense> expenses = expenseRepository.findByBudget(budget);
        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        budget.setTotalExpense(totalExpense);
        updateBalance(budget);
        budgetRepository.save(budget);
    }

    private void updateBalance(Budget budget) {
        budget.setBalance(budget.getTotalIncome().subtract(budget.getTotalExpense()));
        budgetRepository.save(budget);
    }

    private void updateTotalIncome(Budget budget) {
        List<Income> incomes = incomeRepository.findByBudget(budget);
        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        budget.setTotalIncome(totalIncome);
        updateBalance(budget);
        budgetRepository.save(budget);
    }

}




