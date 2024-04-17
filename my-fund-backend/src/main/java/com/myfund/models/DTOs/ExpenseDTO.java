package com.myfund.models.DTO;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
public class ExpenseDTO {

    @NotNull
    private String name;

    @NotNull
    private BigDecimal amount;
}
