package com.myfund.service;

import com.myfund.model.Budget;
import com.myfund.model.DTO.BudgetDTO;
import com.myfund.model.DTO.CreateBudgetDTO;
import com.myfund.model.DTO.mapper.BudgetMapper;
import com.myfund.model.User;
import com.myfund.repository.BudgetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        budget.setNameBudget("Default Budget");
        budget.setUser(user);
        budgetRepository.save(budget);
        log.info("Default empty budget saved for user. Email: {}", user.getEmail());
    }

    public BudgetDTO createBudget(CreateBudgetDTO createBudgetDTO, User user) {
        Budget budget = BudgetMapper.createBudgetDTOMapToBudget(createBudgetDTO);
        budget.setUser(user);
        budgetRepository.save(budget);
        log.info("New budget saved for user. Email: {}. Name: {}", user.getEmail(), budget.getNameBudget());
        return BudgetMapper.budgetMapToBudgetDTO(budget);
    }

}
