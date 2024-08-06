package com.myfund.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChange {

    private String email;

    private String token;

    private String newPassword;
}
