package com.myfund.repositories;

import com.myfund.models.Budget;
import com.myfund.models.Expense;
import com.myfund.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBudget(Budget budget);

    Optional<Expense> findByIdAndUserIdAndBudgetId(Long expenseId, Long userId, Long budgetId);

    List<Expense> findByIdCategory(Long idCategory);

    List<Expense> findByIdSubCategory(Long subcategoryId);

    List<Expense> findByIdCategoryAndUserIdAndBudgetId(Long idCategory, Long userId, Long budgetId);

    List<Expense> findByIdSubCategoryAndUserIdAndBudgetId(Long idSubCategory, Long userId, Long budgetId);

    List<Expense> findByBudgetId(Long budgetId);

    void deleteExpenseByIdAndUserAndBudgetId(Long expenseId, User user, Long budgetId);

    List<Expense> findByBudgetIdAndUser(Long budgetId, User user);
}