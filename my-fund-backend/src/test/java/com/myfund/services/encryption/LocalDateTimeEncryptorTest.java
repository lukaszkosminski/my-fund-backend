package com.myfund.services.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateTimeEncryptorTest {

    private LocalDateTimeEncryptor encryptor;
    private static final String ENCRYPTION_KEY = "testEncryptionKey";
    private static final String ENCRYPTED_DATA = "encryptedData";
    private static final String DECRYPTED_DATA = "2023-10-01T12:00:00";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2023, 10, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        encryptor = new LocalDateTimeEncryptor();
        ReflectionTestUtils.setField(encryptor, "encryptionKey", ENCRYPTION_KEY);
    }

    @Test
    void testConvertToDatabaseColumn_Success() {
        try (MockedStatic<EncryptionUtil> mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class)) {
            mockedEncryptionUtil.when(() -> EncryptionUtil.encrypt(DECRYPTED_DATA, ENCRYPTION_KEY)).thenReturn(ENCRYPTED_DATA);

            String result = encryptor.convertToDatabaseColumn(LOCAL_DATE_TIME);
            assertEquals(ENCRYPTED_DATA, result);
        }
    }

    @Test
    void testConvertToDatabaseColumn_Exception() {
        try (MockedStatic<EncryptionUtil> mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class)) {
            mockedEncryptionUtil.when(() -> EncryptionUtil.encrypt(DECRYPTED_DATA, ENCRYPTION_KEY)).thenThrow(new RuntimeException("Encryption error"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> encryptor.convertToDatabaseColumn(LOCAL_DATE_TIME));
            assertEquals("Error encrypting LocalDateTime", exception.getMessage());
        }
    }

    @Test
    void testConvertToEntityAttribute_Success() {
        try (MockedStatic<EncryptionUtil> mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class)) {
            mockedEncryptionUtil.when(() -> EncryptionUtil.decrypt(ENCRYPTED_DATA, ENCRYPTION_KEY)).thenReturn(DECRYPTED_DATA);

            LocalDateTime result = encryptor.convertToEntityAttribute(ENCRYPTED_DATA);
            assertEquals(LOCAL_DATE_TIME, result);
        }
    }

    @Test
    void testConvertToEntityAttribute_Exception() {
        try (MockedStatic<EncryptionUtil> mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class)) {
            mockedEncryptionUtil.when(() -> EncryptionUtil.decrypt(ENCRYPTED_DATA, ENCRYPTION_KEY)).thenThrow(new RuntimeException("Decryption error"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> encryptor.convertToEntityAttribute(ENCRYPTED_DATA));
            assertEquals("Error decrypting LocalDateTime", exception.getMessage());
        }
    }

    @Test
    void testConvertToDatabaseColumn_NullAttribute() {
        String result = encryptor.convertToDatabaseColumn(null);
        assertEquals(null, result);
    }

    @Test
    void testConvertToEntityAttribute_NullDbData() {
        LocalDateTime result = encryptor.convertToEntityAttribute(null);
        assertEquals(null, result);
    }
}