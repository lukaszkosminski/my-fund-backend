package com.myfund.service;

import com.myfund.model.DTO.CreateExpenseDTO;
import com.myfund.model.DTO.mappers.ExpenseMapper;
import com.myfund.model.Expense;
import com.myfund.repository.ExpenseRepository;
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
