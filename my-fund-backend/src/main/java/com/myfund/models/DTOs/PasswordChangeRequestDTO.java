package com.myfund.models.DTOs;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequestDTO {

    @Email(message = "Invalid email format")
    private String email;
}
