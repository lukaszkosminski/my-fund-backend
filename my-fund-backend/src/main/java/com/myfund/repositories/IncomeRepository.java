package com.myfund.repositories;

import com.myfund.models.Budget;
import com.myfund.models.Expense;
import com.myfund.models.Income;
import com.myfund.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByBudget(Budget budget);

    Optional<Income> findByIdAndUserIdAndBudgetId(Long expenseId, Long userId, Long budgetId);

    List<Income> findByIdCategory(Long idCategory);

    List<Income> findByIdSubCategory(Long subcategoryId);

    List<Income> findByIdCategoryAndUserIdAndBudgetId(Long idCategory, Long userId, Long budgetId);

    List<Income> findByIdSubCategoryAndUserIdAndBudgetId(Long idSubCategory, Long userId, Long budgetId);

    List<Income> findByBudgetId(Long budgetId);

    void deleteIncomeByIdAndUser(Long incomeId, User user);

    void deleteExpenseByIdAndUserAndBudgetId(Long incomeId, User user, Long budgetId);
}
