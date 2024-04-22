package com.myfund.models.DTOs;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
@Getter
@Setter
public class EditExpenseDTO {

    @NotNull
    private String name;

    @NotNull
    private BigDecimal amount;
    @NotNull
    private Long idCategory;
    @NotNull
    private Long idSubCategory;
}
