package com.myfund.models.DTOs;

import lombok.*;

@Data
@Builder
public class UserDTO {

    private String email;

    private String username;
}
