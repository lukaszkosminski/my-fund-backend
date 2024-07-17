package com.myfund.services.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailThrottleServiceTest {

    @Mock
    private CacheManager mockCacheManager;

    @Mock
    private Cache mockCache;

    private EmailThrottleService emailThrottleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockCacheManager.getCache("emailThrottleCache")).thenReturn(mockCache);
        emailThrottleService = new EmailThrottleService(mockCacheManager);
    }

    @Test
    void getEmailCount_ShouldReturnCachedCount_WhenCountExistsInCache() {
        String email = "test@example.com";
        AtomicInteger cachedCount = new AtomicInteger(5);
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(cachedCount);

        AtomicInteger result = emailThrottleService.getEmailCount(email);

        assertEquals(5, result.get(), "Email count should be 5");
    }

    @Test
    void getEmailCount_ShouldReturnZero_WhenCountDoesNotExistInCache() {
        String email = "test@example.com";
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(null);

        AtomicInteger result = emailThrottleService.getEmailCount(email);

        assertEquals(0, result.get(), "Email count should be 0");
    }

    @Test
    void getEmailCount_ShouldReturnNewAtomicInteger_WhenCacheIsEmpty() {
        String email = "test@example.com";
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(null);

        AtomicInteger result = emailThrottleService.getEmailCount(email);

        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.get(), "Email count should be 0");
    }

    @Test
    void getEmailCount_ShouldReturnCachedCount_WhenCacheIsNotEmpty() {
        String email = "test@example.com";
        AtomicInteger cachedCount = new AtomicInteger(3);
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(cachedCount);

        AtomicInteger result = emailThrottleService.getEmailCount(email);

        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.get(), "Email count should be 3");
    }

    @Test
    void incrementEmailCount_ShouldReturnNewAtomicInteger_WhenCacheIsEmpty() {
        String email = "test@example.com";
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(null);

        AtomicInteger result = emailThrottleService.incrementEmailCount(email);

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.get(), "Email count should be initialized to 1");
    }

    @Test
    void incrementEmailCount_ShouldReturnIncrementedCount_WhenCacheIsNotEmpty() {
        String email = "test@example.com";
        AtomicInteger cachedCount = new AtomicInteger(3);
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(cachedCount);

        AtomicInteger result = emailThrottleService.incrementEmailCount(email);

        assertNotNull(result, "Result should not be null");
        assertEquals(4, result.get(), "Email count should be incremented to 4");
    }

    @Test
    void canSendEmail_ShouldReturnTrue_WhenEmailCountIsZero() {
        String email = "test@example.com";
        AtomicInteger cachedCount = new AtomicInteger(0);
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(cachedCount);

        boolean result = emailThrottleService.canSendEmail(email);

        assertTrue(result, "Should be able to send email when count is zero");
    }

    @Test
    void canSendEmail_ShouldReturnTrue_WhenEmailCountIsOne() {
        String email = "test@example.com";
        AtomicInteger cachedCount = new AtomicInteger(1);
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(cachedCount);

        boolean result = emailThrottleService.canSendEmail(email);

        assertTrue(result, "Should be able to send email when count is one");
    }

    @Test
    void canSendEmail_ShouldReturnTrue_WhenEmailCountIsTwo() {
        String email = "test@example.com";
        AtomicInteger cachedCount = new AtomicInteger(2);
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(cachedCount);

        boolean result = emailThrottleService.canSendEmail(email);

        assertTrue(result, "Should be able to send email when count is two");
    }

    @Test
    void canSendEmail_ShouldReturnFalse_WhenEmailCountIsThree() {
        String email = "test@example.com";
        AtomicInteger cachedCount = new AtomicInteger(3);
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(cachedCount);

        boolean result = emailThrottleService.canSendEmail(email);

        assertFalse(result, "Should not be able to send email when count is three");
    }

    @Test
    void canSendEmail_ShouldReturnFalse_WhenEmailCountIsGreaterThanThree() {
        String email = "test@example.com";
        AtomicInteger cachedCount = new AtomicInteger(4);
        when(mockCache.get(email, AtomicInteger.class)).thenReturn(cachedCount);

        boolean result = emailThrottleService.canSendEmail(email);

        assertFalse(result, "Should not be able to send email when count is greater than three");
    }
}