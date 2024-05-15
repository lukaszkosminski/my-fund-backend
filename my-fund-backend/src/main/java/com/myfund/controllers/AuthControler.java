package com.myfund.controllers;

import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class AuthControler {

    private final UserService userService;

    @Autowired
    public AuthControler(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody CreateUserDTO createUserDTO) throws IOException {
        UserDTO userDTO = userService.createUser(createUserDTO);
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    //TODO: forgot password
//    @PostMapping("/forgot-password")
//    public void forgotPasswordUser() {
//    }
}
