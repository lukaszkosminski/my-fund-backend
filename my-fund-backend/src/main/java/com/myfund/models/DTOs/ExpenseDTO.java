package com.myfund.models.DTOs;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseDTO {

    private Long id;

    private String name;

    private BigDecimal amount;

    private Long idCategory;

    private Long idSubCategory;

    private LocalDate localDate;

}
