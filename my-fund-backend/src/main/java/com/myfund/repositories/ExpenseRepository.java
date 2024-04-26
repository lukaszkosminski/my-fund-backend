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

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.budget.id = :budgetId AND e.idCategory = :categoryId AND e.user.id = :userId")
    BigDecimal sumExpensesByBudgetIdAndCategoryIdAndUserId(@Param("budgetId") Long budgetId, @Param("categoryId") Long categoryId, @Param("userId") Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.budget.id = :budgetId AND e.idSubCategory = :subcategoryId AND e.user.id = :userId")
    BigDecimal sumExpensesByBudgetIdAndSubcategoryIdAndUserId(@Param("budgetId") Long budgetId, @Param("subcategoryId") Long subcategoryId, @Param("userId") Long userId);

    List<Expense> findByBudgetId(Long budgetId);

    void deleteExpenseByIdAndUserAndBudgetId(Long expenseId, User user, Long budgetId);
}