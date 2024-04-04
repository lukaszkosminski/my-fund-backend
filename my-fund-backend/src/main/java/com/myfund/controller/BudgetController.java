package com.myfund.controller;

import com.myfund.model.DTO.BudgetDTO;
import com.myfund.model.DTO.CreateBudgetDTO;
import com.myfund.model.User;
import com.myfund.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BudgetController {

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/create-budget")
    public BudgetDTO createBudget(@RequestBody CreateBudgetDTO createBudgetDTO, @AuthenticationPrincipal User user) {
        return budgetService.createBudget(createBudgetDTO, user);
    }
    // TODO: Add more endpoints
}