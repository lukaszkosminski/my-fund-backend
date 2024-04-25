package com.myfund.repositories;

import com.myfund.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income,Long> {
    List<Income> findByBudget(Budget budget);

    Optional<Income> findByIdAndUserIdAndBudgetId(Long expenseId, Long userId, Long budgetId);

    List<Income> findByIdCategory(Long idCategory);

    List<Income> findByIdSubCategory(Long subcategoryId);
}
