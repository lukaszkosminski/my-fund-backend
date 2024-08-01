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
        CreateIncomeDTO createIncomeDTO = new CreateIncomeDTO();
        createIncomeDTO.setName("Salary");
        createIncomeDTO.setAmount(new BigDecimal("5000"));
        createIncomeDTO.setIdCategory(1L);
        createIncomeDTO.setIdSubCategory(2L);

        Income income = IncomeMapper.toIncome(createIncomeDTO);

        assertNotNull(income);
        assertEquals("Salary", income.getName());
        assertEquals(new BigDecimal("5000"), income.getAmount());
        assertEquals(1L, income.getIdCategory());
        assertEquals(2L, income.getIdSubCategory());
    }

    @Test
    void testIncomeMapToIncomeDTO() {
        Income income = new Income();
        income.setId(1L);
        income.setName("Salary");
        income.setAmount(new BigDecimal("5000"));
        income.setIdCategory(1L);
        income.setIdSubCategory(2L);
        income.setLocalDateTime(LocalDateTime.of(2023, 10, 1, 10, 0));

        IncomeDTO incomeDTO = IncomeMapper.toIncomeDTO(income);

        assertNotNull(incomeDTO);
        assertEquals(1L, incomeDTO.getId());
        assertEquals("Salary", incomeDTO.getName());
        assertEquals(new BigDecimal("5000"), incomeDTO.getAmount());
        assertEquals(1L, incomeDTO.getIdCategory());
        assertEquals(2L, incomeDTO.getIdSubCategory());
        assertEquals(LocalDateTime.of(2023, 10, 1, 10, 0).toLocalDate(), incomeDTO.getLocalDate());
    }
}