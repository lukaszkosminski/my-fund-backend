package com.myfund.models.DTOs;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
public class SubCategoryDTO {

    private Long id;

    private String name;

}
