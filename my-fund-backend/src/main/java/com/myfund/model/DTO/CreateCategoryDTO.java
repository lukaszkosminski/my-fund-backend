package com.myfund.model.DTO;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
@Getter
@Setter
public class CreateCategoryDTO {
    @NotNull
    private String name;

}
