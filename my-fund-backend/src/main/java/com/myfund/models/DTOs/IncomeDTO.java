package com.myfund.models.DTOs;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class IncomeDTO {

    private Long id;

    private String name;

    private BigDecimal amount;

    private Long idCategory;

    private Long idSubCategory;

    private LocalDateTime localDateTime;
}
