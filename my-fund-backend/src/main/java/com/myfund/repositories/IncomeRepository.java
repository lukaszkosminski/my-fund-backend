package com.myfund.repositories;

import com.myfund.models.Budget;
import com.myfund.models.Expense;
import com.myfund.models.Income;
import com.myfund.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income,Long> {
    List<Income> findByBudget(Budget budget);

    Optional<Income> findByIdAndUser(Long incomeId, User user);
}
