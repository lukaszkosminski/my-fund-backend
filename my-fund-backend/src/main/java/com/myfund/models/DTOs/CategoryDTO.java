package com.myfund.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
public class CategoryDTO {

    private Long id;

    private String name;

    private List<SubCategoryDTO> subCategories;

}
