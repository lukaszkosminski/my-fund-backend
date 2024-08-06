package com.myfund.integration;

import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.PasswordChangeDTO;
import com.myfund.models.DTOs.PasswordChangeRequestDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.DTOs.mappers.PasswordChangeMapper;
import com.myfund.models.DTOs.mappers.PasswordChangeRequestMapper;
import com.myfund.models.DTOs.mappers.UserMapper;
import com.myfund.models.User;
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
        User user = userService.createUser(UserMapper.toModel(createUserDTO));
        return new ResponseEntity<>(UserMapper.toDTO(user), HttpStatus.CREATED);
    }

    @PostMapping("/request-change-password")
    public ResponseEntity<?> requestChangePassword(@Valid @RequestBody PasswordChangeRequestDTO passwordChangeRequestDTO) {
        userService.requestPasswordChange(PasswordChangeRequestMapper.toModel(passwordChangeRequestDTO));
        return new ResponseEntity<>(passwordChangeRequestDTO, HttpStatus.ACCEPTED);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(PasswordChangeMapper.toModel(passwordChangeDTO));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}