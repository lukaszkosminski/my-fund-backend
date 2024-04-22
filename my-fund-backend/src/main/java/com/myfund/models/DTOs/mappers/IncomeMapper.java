package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateIncomeDTO;
import com.myfund.models.DTOs.IncomeDTO;
import com.myfund.models.Income;

public class IncomeMapper {

    public static Income createIncomeDTOtoIncome(CreateIncomeDTO createIncomeDTO) {
        Income income = new Income();
        income.setName(createIncomeDTO.getName());
        income.setAmount(createIncomeDTO.getAmount());
        income.setIdCategory(createIncomeDTO.getIdCategory());
        income.setIdSubCategory(createIncomeDTO.getIdSubCategory());
        return income;
    }

    public static IncomeDTO incomeMapToIncomeDTO(Income income) {
        IncomeDTO incomeDTO = new IncomeDTO();
        incomeDTO.setId(income.getId());
        incomeDTO.setName(income.getName());
        incomeDTO.setAmount(income.getAmount());
        incomeDTO.setIdCategory(income.getIdCategory());
        incomeDTO.setIdSubCategory(income.getIdSubCategory());
        return incomeDTO;
    }
}
