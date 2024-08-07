package com.myfund.models.DTOs;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateCategoryDTO {
    @NotNull
    private String name;

    private List<CreateSubCategoryDTO> subCategories;

}
