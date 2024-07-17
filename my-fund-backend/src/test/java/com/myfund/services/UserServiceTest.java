package com.myfund.services;

import com.myfund.exceptions.InvalidTokenException;
import com.myfund.exceptions.UserAlreadyExistsException;
import com.myfund.exceptions.UserNotFoundException;
import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.PasswordChangeDTO;
import com.myfund.models.DTOs.PasswordChangeRequestDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.models.DTOs.mappers.UserMapper;
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
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setPassword("password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        UserDTO userDTO = userService.createUser(createUserDTO);

        assertNotNull(userDTO);
        verify(userRepository, times(1)).save(any(User.class));
        verify(budgetService, times(1)).createDefaultBudget(any(User.class));
        verify(emailSender, times(1)).sendWelcomeEmail(any(UserDTO.class));
    }

    @Test
    void createUser_ShouldThrowExceptionForExistingUsername() {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setEmail("test@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserDTO));
    }

    @Test
    void requestPasswordChange_ShouldSendPasswordResetEmail() throws IOException {
        PasswordChangeRequestDTO passwordChangeRequestDTO = new PasswordChangeRequestDTO();
        passwordChangeRequestDTO.setEmail("test@example.com");

        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenService.createPasswordResetToken("test@example.com")).thenReturn("resetToken");

        userService.requestPasswordChange(passwordChangeRequestDTO);

        verify(emailSender, times(1)).sendPasswordResetEmail(any(UserDTO.class), eq("resetToken"));
    }

    @Test
    void changePassword_ShouldChangePasswordSuccessfully() {
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setEmail("test@example.com");
        passwordChangeDTO.setToken("validToken");
        passwordChangeDTO.setNewPassword("newPassword");

        User user = new User();
        user.setEmail("test@example.com");

        when(tokenService.getPasswordResetToken("test@example.com")).thenReturn("validToken");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.changePassword(passwordChangeDTO);

        verify(userRepository, times(1)).save(user);
        verify(tokenService, times(1)).invalidatePasswordResetToken("test@example.com");
    }

    @Test
    void changePassword_ShouldThrowExceptionForInvalidToken() {
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setEmail("test@example.com");
        passwordChangeDTO.setToken("invalidToken");
        passwordChangeDTO.setNewPassword("newPassword");

        when(tokenService.getPasswordResetToken("test@example.com")).thenReturn("validToken");

        assertThrows(InvalidTokenException.class, () -> userService.changePassword(passwordChangeDTO));
    }
}