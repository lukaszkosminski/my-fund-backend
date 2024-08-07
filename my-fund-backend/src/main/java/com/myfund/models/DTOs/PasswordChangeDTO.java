package com.myfund.models.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class PasswordChangeDTO {

    @Email(message = "Invalid email format")
    private String email;

    private String token;

    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String newPassword;

}
