package com.myfund.service;

import com.myfund.model.Budget;
import com.myfund.model.DTO.BudgetDTO;
import com.myfund.model.DTO.CreateBudgetDTO;
import com.myfund.model.DTO.mappers.BudgetMapper;
import com.myfund.model.User;
import com.myfund.repository.BudgetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public void createDefaultBudget(User user) {
        Budget budget = new Budget();
        budget.setName("Default Budget");
        budget.setUser(user);
        budget.setLocalDateTime(LocalDateTime.now());
        budgetRepository.save(budget);
        log.info("Default empty budget saved for user. Email: {}", user.getEmail());
    }

    public BudgetDTO createBudget(CreateBudgetDTO createBudgetDTO, User user) {
        Budget budget = BudgetMapper.createBudgetDTOMapToBudget(createBudgetDTO);
        budget.setUser(user);
        budget.setLocalDateTime(LocalDateTime.now());
        budgetRepository.save(budget);
        log.info("New budget saved for user. Email: {}. Name: {}", user.getEmail(), budget.getName());
        return BudgetMapper.budgetMapToBudgetDTO(budget);
    }

}
