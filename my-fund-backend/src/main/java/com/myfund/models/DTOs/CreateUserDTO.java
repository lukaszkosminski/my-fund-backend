package com.myfund.models.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class CreateUserDTO {
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String password;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 4, message = "User name must be at least 4 characters long")
    private String username;
}
