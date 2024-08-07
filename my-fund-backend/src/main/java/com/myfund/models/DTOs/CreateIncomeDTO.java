package com.myfund.models.DTOs;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
@Data
@Builder
public class CreateIncomeDTO {

    @NotNull
    private String name;

    @NotNull
    private BigDecimal amount;

    private Long idCategory;

    private Long idSubCategory;
}
