package com.myfund.models.DTOs;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class IncomeDTO {

    private Long id;

    private String name;

    private BigDecimal amount;

    private Long idCategory;

    private Long idSubCategory;

    private LocalDate localDate;
}
