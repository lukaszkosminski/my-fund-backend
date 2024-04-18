package com.myfund.controllers;

import com.myfund.models.DTOs.BudgetDTO;
import com.myfund.models.DTOs.CreateBudgetDTO;
import com.myfund.models.User;
import com.myfund.services.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class BudgetController {

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/budgets")
    public BudgetDTO createBudget(@RequestBody CreateBudgetDTO createBudgetDTO, @AuthenticationPrincipal User user) {
        return budgetService.createBudget(createBudgetDTO, user);
    }
    // TODO: Add more endpoints
}