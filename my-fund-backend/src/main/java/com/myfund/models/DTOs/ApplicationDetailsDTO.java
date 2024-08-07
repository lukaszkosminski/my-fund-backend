package com.myfund.models.DTOs;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ApplicationDetailsDTO {

    private String version;

    private String buildDate;
}
