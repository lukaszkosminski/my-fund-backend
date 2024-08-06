package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateUserDTO;
//import com.myfund.models.DTOs.EditUserDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.User;

public class UserMapper {

    public static User toModel(CreateUserDTO createUserDTO) {
        return User.builder()
                .email(createUserDTO.getEmail())
                .password(createUserDTO.getPassword())
                .username(createUserDTO.getUsername())
                .build();
    }

    public static UserDTO toDTO(User user) {
        return UserDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
