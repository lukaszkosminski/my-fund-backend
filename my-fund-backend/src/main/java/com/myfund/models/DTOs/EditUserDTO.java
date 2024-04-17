package com.myfund.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditUserDTO {

    private String password;
    private String email;
    private String usename;
}
