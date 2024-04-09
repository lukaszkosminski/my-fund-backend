package com.myfund.controller;

import com.myfund.model.DTO.UserDTO;
import com.myfund.model.DTO.mapper.UserMapper;
import com.myfund.model.User;
import com.myfund.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current-user")
    @ResponseBody
    public UserDTO getCurrentUser(@AuthenticationPrincipal User user) {
        return UserMapper.userMapToUserDTO(user);
    }
    // TODO: Add more endpoints
}
