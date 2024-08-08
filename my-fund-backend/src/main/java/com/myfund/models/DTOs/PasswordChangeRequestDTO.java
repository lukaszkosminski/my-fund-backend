package com.myfund.models.DTOs;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class PasswordChangeRequestDTO {

    @Email(message = "Invalid email format")
    private String email;
}
