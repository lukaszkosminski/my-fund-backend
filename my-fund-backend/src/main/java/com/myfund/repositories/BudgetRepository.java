package com.myfund.repository;

import com.myfund.model.Budget;
import com.myfund.model.DTO.BudgetDTO;
import com.myfund.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
}
