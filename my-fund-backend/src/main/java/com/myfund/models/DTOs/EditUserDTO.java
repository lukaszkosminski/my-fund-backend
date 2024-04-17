package com.myfund.models.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditUserDTO {

    private String password;
    private String email;
    private String usename;
}
