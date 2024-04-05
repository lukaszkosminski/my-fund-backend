package com.myfund.controller;

import com.myfund.model.DTO.CreateUserDTO;
import com.myfund.model.DTO.UserDTO;
import com.myfund.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class AuthControler {

    private final UserService userService;
    @Autowired
    public AuthControler(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserDTO registereUser(@RequestBody CreateUserDTO createUserDTO) {
        return userService.createUser(createUserDTO);
    }
    //TODO: forgot password
    @PostMapping("/forgot-password")
    public void forgotPasswordUser() {
    }



}
