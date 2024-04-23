package com.myfund.repositories;

import com.myfund.models.Budget;
import com.myfund.models.Expense;
import com.myfund.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBudget(Budget budget);

    Optional<Expense> findByIdAndUserIdAndBudgetId(Long expenseId, Long userId, Long budgetId);
}
