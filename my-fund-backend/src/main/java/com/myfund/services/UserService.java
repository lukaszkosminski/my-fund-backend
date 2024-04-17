package com.myfund.services;

import com.myfund.exceptions.UserAlreadyExistsException;
import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.DTOs.mappers.UserMapper;
import com.myfund.models.User;
import com.myfund.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BudgetService budgetService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BudgetService budgetService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.budgetService = budgetService;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserDTO> createUser(CreateUserDTO createUserDTO) {
        Optional<User> userOpt = userRepository.findByUsernameAndEmail(createUserDTO.getUsername(), createUserDTO.getEmail());
        if (userOpt.isPresent()) {
            throw new UserAlreadyExistsException("user is not unique");
        } else {
            User user = UserMapper.createUserDTOMapToUser(createUserDTO);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("USER");
            log.info("User saved successfully. Email: {}, Pass: {}, ", user.getEmail(), maskHash(user.getPassword()));
            userRepository.save(user);
            budgetService.createDefaultBudget(user);
            return Optional.of(UserMapper.userMapToUserDTO(user));
        }
    }

    private String maskHash(String hash) {
        String firstFourChars = hash.substring(0, 4);
        String lastFourChars = hash.substring(hash.length() - 4);
        return firstFourChars + "(...)" + lastFourChars;
    }

}
