package com.myfund.controllers;

import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestResetPassword(@RequestParam("email") String email) {
        userService.requestPasswordReset(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "If your email address exists in our database, you will receive a password reset email shortly.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("email") String email, @RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        userService.resetPassword(email, token, newPassword);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password has been successfully reset.");
        return ResponseEntity.ok(response);
    }
}
