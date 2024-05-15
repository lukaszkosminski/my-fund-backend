package com.myfund.services;

import com.myfund.exceptions.UserAlreadyExistsException;
import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.DTOs.mappers.UserMapper;
import com.myfund.models.User;
import com.myfund.repositories.UserRepository;
import com.myfund.services.email.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final BudgetService budgetService;

    private final PasswordEncoder passwordEncoder;

    private final EmailSender emailSender;

    @Autowired
    public UserService(UserRepository userRepository, BudgetService budgetService, PasswordEncoder passwordEncoder, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.budgetService = budgetService;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
    }

    public UserDTO createUser(CreateUserDTO createUserDTO) throws IOException {

        validateUniqueness(createUserDTO.getUsername(), createUserDTO.getEmail());

        User user = UserMapper.createUserDTOMapToUser(createUserDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        log.info("User saved successfully. Email: {}, Pass: {}, ", user.getEmail(), maskHash(user.getPassword()));
        userRepository.save(user);
        budgetService.createDefaultBudget(user);
        emailSender.sendWelcomeEmail(UserMapper.userMapToUserDTO(user));
        return UserMapper.userMapToUserDTO(user);
    }

    private String maskHash(String hash) {
        String firstFourChars = hash.substring(0, 4);
        String lastFourChars = hash.substring(hash.length() - 4);
        return firstFourChars + "(...)" + lastFourChars;
    }

    private void validateUniqueness(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Attempt to create a user with an existing username: {}", username);
            throw new UserAlreadyExistsException("Username is not unique");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Attempt to create a user with an existing email: {}", email);
            throw new UserAlreadyExistsException("Email is not unique");
        }
    }
}
