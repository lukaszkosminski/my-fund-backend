package com.myfund.controllers;

import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.PasswordChangeRequestDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

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

    @PostMapping("/request-change-password")
    public ResponseEntity<?> requestChangePassword(@Valid @RequestBody PasswordChangeRequestDTO passwordChangeRequestDTO) {
        userService.requestPasswordChange(passwordChangeRequestDTO.getEmail());
        return ResponseEntity.ok(HttpStatus.ACCEPTED);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam("email") String email, @RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        userService.changePassword(email, token, newPassword);
        return ResponseEntity.ok(HttpStatus.ACCEPTED);
    }
}
