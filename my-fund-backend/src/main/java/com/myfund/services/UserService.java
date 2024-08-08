package com.myfund.services;

import com.myfund.exceptions.InvalidTokenException;
import com.myfund.exceptions.UserAlreadyExistsException;
import com.myfund.exceptions.UserNotFoundException;
import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.PasswordChangeDTO;
import com.myfund.models.DTOs.PasswordChangeRequestDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.DTOs.mappers.UserMapper;
import com.myfund.models.PasswordChange;
import com.myfund.models.PasswordChangeRequest;
import com.myfund.models.User;
import com.myfund.repositories.UserRepository;
import com.myfund.services.email.EmailSender;
import com.myfund.services.email.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final BudgetService budgetService;

    private final PasswordEncoder passwordEncoder;

    private final EmailSender emailSender;

    private final TokenService tokenService;

    private final CacheManager cacheManager;

    public User createUser(User user) throws IOException {
        validateUniqueness(user.getUsername(), user.getEmail());

        User inicializedUser = User.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .role("USER")
                .build();
        log.info("User saved successfully. Email: {}, Pass: {}, ", user.getEmail(), maskHash(user.getPassword()));
        userRepository.save(inicializedUser);
        budgetService.createDefaultBudget(inicializedUser);
        emailSender.sendWelcomeEmail(inicializedUser);
        return user;
    }

    private String maskHash(String hash) {
        String firstFourChars = hash.substring(0, 4);
        String lastFourChars = hash.substring(hash.length() - 4);
        return firstFourChars + "(...)" + lastFourChars;
    }

    private void validateUniqueness(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            log.info("Attempt to create a user with an existing username: {}", username);
            throw new UserAlreadyExistsException("Username is not unique");
        }
        if (userRepository.existsByEmail(email)) {
            log.info("Attempt to create a user with an existing email: {}", email);
            throw new UserAlreadyExistsException("Email is not unique");
        }
    }

    public void requestPasswordChange(PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findByEmail(passwordChangeRequest.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", passwordChangeRequest.getEmail());
                    return new UserNotFoundException("User not found for email: " + passwordChangeRequest.getEmail());
                });

        String passwordResetToken = tokenService.createPasswordResetToken(user.getEmail());
        try {
            emailSender.sendPasswordResetEmail(user, passwordResetToken);
        } catch (IOException e) {
            log.error("Failed to send password reset email", e);
        }

        log.debug("Request for password reset received for email: {}, but no action taken.", passwordChangeRequest.getEmail());
    }

    public void changePassword(PasswordChange passwordChange) {

        log.info("Attempting to reset password for email: {}", passwordChange.getEmail());
        String cachedToken = tokenService.getPasswordResetToken(passwordChange.getEmail());

        if (cachedToken == null || !cachedToken.equals(passwordChange.getToken())) {
            log.error("Invalid or expired token for email: {}", passwordChange.getEmail());
            throw new InvalidTokenException("Invalid token");
        }

        log.debug("Token validation successful for email: {}", passwordChange.getEmail());

        User user = userRepository.findByEmail(passwordChange.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", passwordChange.getEmail());
                    return new UserNotFoundException("User not found for email: " + passwordChange.getEmail());
                });

        user.setPassword(passwordEncoder.encode(passwordChange.getNewPassword()));
        userRepository.save(user);
        tokenService.invalidatePasswordResetToken(passwordChange.getEmail());
        log.info("Password reset successful for email: {}", passwordChange.getEmail());
    }
}