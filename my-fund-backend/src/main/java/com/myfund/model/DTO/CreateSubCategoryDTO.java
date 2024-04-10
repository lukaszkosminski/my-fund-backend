package com.myfund.model.DTO;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
@Setter
@Getter
public class CreateSubCategoryDTO {

    @NotNull
    private String name;
}
