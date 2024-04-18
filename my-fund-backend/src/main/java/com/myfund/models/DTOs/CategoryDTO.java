package com.myfund.models.DTOs;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class CategoryDTO {

    private Long id;

    @NotNull
    private String name;

    private List<SubCategoryDTO> subCategories;

}
