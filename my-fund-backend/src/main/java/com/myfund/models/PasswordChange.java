package com.myfund.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class PasswordChange {

    private String email;

    private String token;

    private String newPassword;
}
