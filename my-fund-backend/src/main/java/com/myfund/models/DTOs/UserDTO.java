package com.myfund.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class UserDTO {

    private String email;

    private String username;
}
