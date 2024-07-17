package com.myfund.services.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private CacheManager cacheManager;

    @Test
    void createPasswordResetToken_WithValidEmail_ShouldReturnToken() {
        String email = "test@example.com";
        String token = tokenService.createPasswordResetToken(email);
        assertNotNull(token, "Token should not be null");
        assertEquals(36, token.length(), "Token should be 36 characters long");
    }
}