package com.myfund.repositories;

import com.myfund.models.Budget;
import com.myfund.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByNameAndUser(String name, User user);

    Optional<Budget> findByIdAndUser(Long budgetId, User user);

    List<Budget> findAllCategoriesByUser(User user);

    List<Budget> findAllByUser(User user);

    void deleteBudgetByIdAndUser(Long budgetId, User user);
}
