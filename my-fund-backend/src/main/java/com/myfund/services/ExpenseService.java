package com.myfund.service;

import com.myfund.models.DTO.CreateExpenseDTO;
import com.myfund.models.DTO.mappers.ExpenseMapper;
import com.myfund.models.Expense;
import com.myfund.repositories.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public Expense createExpense(CreateExpenseDTO createExpenseDTO) {
        Expense expense = ExpenseMapper.createExpenseDTOtoExpense(createExpenseDTO);
        return expenseRepository.save(expense);
    }

}
