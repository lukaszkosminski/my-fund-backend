package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateIncomeDTO;
import com.myfund.models.DTOs.IncomeDTO;
import com.myfund.models.Income;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IncomeMapperTest {

    @Test
    void testCreateIncomeDTOtoIncome() {

        CreateIncomeDTO createIncomeDTO = CreateIncomeDTO
                .builder()
                .name("Salary")
                .amount(new BigDecimal("5000"))
                .idCategory(1L)
                .idSubCategory(2L)
                .build();

        Income income = IncomeMapper.toModel(createIncomeDTO);

        assertNotNull(income);
        assertEquals("Salary", income.getName());
        assertEquals(new BigDecimal("5000"), income.getAmount());
        assertEquals(1L, income.getIdCategory());
        assertEquals(2L, income.getIdSubCategory());
    }

    @Test
    void testIncomeMapToIncomeDTO() {
        Income income = Income.builder()
                .id(1L)
                .name("Salary")
                .amount(new BigDecimal("5000"))
                .idCategory(1L)
                .idSubCategory(2L)
                .localDateTime(LocalDateTime.of(2023, 10, 1, 10, 0))
                .build();

        IncomeDTO incomeDTO = IncomeMapper.toDTO(income);

        assertNotNull(incomeDTO);
        assertEquals(1L, incomeDTO.getId());
        assertEquals("Salary", incomeDTO.getName());
        assertEquals(new BigDecimal("5000"), incomeDTO.getAmount());
        assertEquals(1L, incomeDTO.getIdCategory());
        assertEquals(2L, incomeDTO.getIdSubCategory());
        assertEquals(LocalDateTime.of(2023, 10, 1, 10, 0).toLocalDate(), incomeDTO.getLocalDate());
    }
}