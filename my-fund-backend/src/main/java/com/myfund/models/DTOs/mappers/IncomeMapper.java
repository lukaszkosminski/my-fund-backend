package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateIncomeDTO;
import com.myfund.models.DTOs.IncomeDTO;
import com.myfund.models.Income;

public class IncomeMapper {

    public static Income toModel(CreateIncomeDTO createIncomeDTO) {
        Income income = Income.builder()
                .name(createIncomeDTO.getName())
                .amount(createIncomeDTO.getAmount())
                .idCategory(createIncomeDTO.getIdCategory())
                .idSubCategory(createIncomeDTO.getIdSubCategory())
                .build();
        return income;
    }

    public static IncomeDTO toDTO(Income income) {
        return IncomeDTO.builder()
                .id(income.getId())
                .name(income.getName())
                .amount(income.getAmount())
                .idCategory(income.getIdCategory())
                .idSubCategory(income.getIdSubCategory())
                .localDate(income.getLocalDateTime().toLocalDate())
                .build();
    }
}
