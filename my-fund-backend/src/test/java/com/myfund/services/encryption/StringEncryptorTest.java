package com.myfund.services.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class StringEncryptorTest {

    private StringEncryptor stringEncryptor;
    private static final String ENCRYPTION_KEY = "testKey";

    @BeforeEach
    void setUp() {
        stringEncryptor = new StringEncryptor();
        ReflectionTestUtils.setField(stringEncryptor, "encryptionKey", ENCRYPTION_KEY);
    }

    @Test
    void convertToDatabaseColumn_ShouldEncryptString() {
        try (MockedStatic<EncryptionUtil> mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class)) {
            mockedEncryptionUtil.when(() -> EncryptionUtil.encrypt(anyString(), anyString())).thenReturn("encryptedString");

            String result = stringEncryptor.convertToDatabaseColumn("testString");

            assertEquals("encryptedString", result);
            mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt("testString", ENCRYPTION_KEY), times(1));
        }
    }

    @Test
    void convertToDatabaseColumn_ShouldThrowRuntimeExceptionOnEncryptionError() {
        try (MockedStatic<EncryptionUtil> mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class)) {
            mockedEncryptionUtil.when(() -> EncryptionUtil.encrypt(anyString(), anyString())).thenThrow(new RuntimeException("Encryption error"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                stringEncryptor.convertToDatabaseColumn("testString");
            });

            assertEquals("Error encrypting string", exception.getMessage());
        }
    }

    @Test
    void convertToEntityAttribute_ShouldDecryptString() {
        try (MockedStatic<EncryptionUtil> mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class)) {
            mockedEncryptionUtil.when(() -> EncryptionUtil.decrypt(anyString(), anyString())).thenReturn("decryptedString");

            String result = stringEncryptor.convertToEntityAttribute("encryptedString");

            assertEquals("decryptedString", result);
            mockedEncryptionUtil.verify(() -> EncryptionUtil.decrypt("encryptedString", ENCRYPTION_KEY), times(1));
        }
    }

    @Test
    void convertToEntityAttribute_ShouldThrowRuntimeExceptionOnDecryptionError() {
        try (MockedStatic<EncryptionUtil> mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class)) {
            mockedEncryptionUtil.when(() -> EncryptionUtil.decrypt(anyString(), anyString())).thenThrow(new RuntimeException("Decryption error"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                stringEncryptor.convertToEntityAttribute("encryptedString");
            });

            assertEquals("Error decrypting string", exception.getMessage());
        }
    }

    @Test
    void convertToDatabaseColumn_ShouldHandleNullAttribute() {
        String result = stringEncryptor.convertToDatabaseColumn(null);
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute_ShouldHandleNullDbData() {
        String result = stringEncryptor.convertToEntityAttribute(null);
        assertNull(result);
    }
}