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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailSender emailSender;

    @Mock
    private TokenService tokenService;

    @Mock
    private CacheManager cacheManager;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, budgetService, passwordEncoder, emailSender, tokenService, cacheManager);
    }

    @Test
    void createUser_ShouldCreateUserSuccessfully() throws IOException {
         User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        User userCreated = userService.createUser(user);

        assertNotNull(userCreated);
        verify(userRepository, times(1)).save(any(User.class));
        verify(budgetService, times(1)).createDefaultBudget(any(User.class));
        verify(emailSender, times(1)).sendWelcomeEmail(any(User.class));
    }

    @Test
    void createUser_ShouldThrowExceptionForExistingUsername() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(user));
    }

    @Test
    void requestPasswordChange_ShouldSendPasswordResetEmail() throws IOException {
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setEmail("test@example.com");

        User user = User.builder()
                .email("test@example.com")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenService.createPasswordResetToken("test@example.com")).thenReturn("resetToken");

        userService.requestPasswordChange(passwordChangeRequest);

        verify(emailSender, times(1)).sendPasswordResetEmail(any(User.class), eq("resetToken"));
    }

    @Test
    void changePassword_ShouldChangePasswordSuccessfully() {
        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setEmail("test@example.com");
        passwordChange.setToken("validToken");
        passwordChange.setNewPassword("newPassword");

        User user = User.builder()
                .email("test@example.com")
                .build();

        when(tokenService.getPasswordResetToken("test@example.com")).thenReturn("validToken");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.changePassword(passwordChange);

        verify(userRepository, times(1)).save(user);
        verify(tokenService, times(1)).invalidatePasswordResetToken("test@example.com");
    }

    @Test
    void changePassword_ShouldThrowExceptionForInvalidToken() {
        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setEmail("test@example.com");
        passwordChange.setToken("invalidToken");
        passwordChange.setNewPassword("newPassword");

        when(tokenService.getPasswordResetToken("test@example.com")).thenReturn("validToken");

        assertThrows(InvalidTokenException.class, () -> userService.changePassword(passwordChange));
    }
}