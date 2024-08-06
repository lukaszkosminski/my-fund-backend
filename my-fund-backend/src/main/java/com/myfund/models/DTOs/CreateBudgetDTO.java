package com.myfund.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CreateBudgetDTO {
    @NotNull
    private String name;
}