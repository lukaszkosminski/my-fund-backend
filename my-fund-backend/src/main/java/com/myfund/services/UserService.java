package com.myfund.services;

import com.myfund.exceptions.InvalidTokenException;
import com.myfund.exceptions.UserAlreadyExistsException;
import com.myfund.exceptions.UserNotFoundException;
import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.DTOs.mappers.UserMapper;
import com.myfund.models.User;
import com.myfund.repositories.UserRepository;
import com.myfund.services.email.EmailSender;
import com.myfund.services.email.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final BudgetService budgetService;

    private final PasswordEncoder passwordEncoder;

    private final EmailSender emailSender;

    private final TokenService tokenService;

    private final CacheManager cacheManager;


    @Autowired
    public UserService(UserRepository userRepository, BudgetService budgetService, PasswordEncoder passwordEncoder, EmailSender emailSender, TokenService tokenService, CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.budgetService = budgetService;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.tokenService = tokenService;
        this.cacheManager = cacheManager;
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
            log.info("Attempt to create a user with an existing username: {}", username);
            throw new UserAlreadyExistsException("Username is not unique");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Attempt to create a user with an existing email: {}", email);
            throw new UserAlreadyExistsException("Email is not unique");
        }
    }

    public void requestPasswordChange(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            String passwordResetToken = tokenService.createPasswordResetToken(userOpt.get().getEmail());
            try {
                UserDTO userDTO = UserMapper.userMapToUserDTO(userOpt.get());
                emailSender.sendPasswordResetEmail(userDTO, passwordResetToken);
            } catch (IOException e) {
                log.error("Failed to send password reset email", e);
            }
        } else {
            log.debug("Request for password reset received for email: {}, but no action taken.", email);
        }
    }

    public void changePassword(String email, String token, String newPassword) {

        log.info("Attempting to reset password for email: {}", email);
        String cachedToken = tokenService.getPasswordResetToken(email);

        if (cachedToken != null && cachedToken.equals(token)) {
            log.debug("Token validation successful for email: {}", email);
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                tokenService.invalidatePasswordResetToken(email);
                log.info("Password reset successful for email: {}", email);
            } else {
                log.error("User not found for email: {}", email);
                throw new UserNotFoundException("User not found");
            }
        } else {
            log.error("Invalid or expired token for email: {}", email);
            throw new InvalidTokenException("Invalid token");
        }
    }
}
