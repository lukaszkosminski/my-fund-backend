package com.myfund.repositories;

import com.myfund.models.Budget;
import com.myfund.models.Income;
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

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.budget.id = :budgetId AND i.idCategory = :categoryId AND i.user.id = :userId")
    BigDecimal sumIncomesByBudgetIdAndCategoryIdAndUserId(@Param("budgetId") Long budgetId, @Param("categoryId") Long categoryId, @Param("userId") Long userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.budget.id = :budgetId AND i.idSubCategory = :subcategoryId AND i.user.id = :userId")
    BigDecimal sumIncomesByBudgetIdAndSubcategoryIdAndUserId(@Param("budgetId") Long budgetId, @Param("subcategoryId") Long subcategoryId, @Param("userId") Long userId);

    List<Income> findByBudgetId(Long budgetId);
}
