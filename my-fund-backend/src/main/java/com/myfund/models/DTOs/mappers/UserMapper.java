package com.myfund.models.DTOs.mappers;

import com.myfund.models.DTOs.CreateUserDTO;
//import com.myfund.models.DTOs.EditUserDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.User;

public class UserMapper {

    public static User toUser(CreateUserDTO createUserDTO) {
        User user = new User();
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(createUserDTO.getPassword());
        user.setUsername(createUserDTO.getUsername());
        return user;
    }

    public static UserDTO toUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        return userDTO;
    }
}
