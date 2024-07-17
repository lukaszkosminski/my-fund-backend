package com.myfund.services.encryption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class BigDecimalEncryptorTest {

    private BigDecimalEncryptor bigDecimalEncryptor;
    private MockedStatic<EncryptionUtil> encryptionUtilMock;

    @BeforeEach
    void setUp() {
        bigDecimalEncryptor = new BigDecimalEncryptor();
        ReflectionTestUtils.setField(bigDecimalEncryptor, "encryptionKey", "testKey");
        encryptionUtilMock = mockStatic(EncryptionUtil.class);
    }

    @AfterEach
    void tearDown() {
        encryptionUtilMock.close();
    }

    @Test
    void convertToDatabaseColumn_ShouldEncryptBigDecimal() {
        BigDecimal attribute = new BigDecimal("123.45");
        encryptionUtilMock.when(() -> EncryptionUtil.encrypt(anyString(), anyString())).thenReturn("encryptedValue");

        String encryptedValue = bigDecimalEncryptor.convertToDatabaseColumn(attribute);

        assertEquals("encryptedValue", encryptedValue);
        encryptionUtilMock.verify(() -> EncryptionUtil.encrypt("123.45", "testKey"), Mockito.times(1));
    }

    @Test
    void convertToDatabaseColumn_ShouldThrowRuntimeExceptionOnEncryptionError() {
        BigDecimal attribute = new BigDecimal("123.45");
        encryptionUtilMock.when(() -> EncryptionUtil.encrypt(anyString(), anyString())).thenThrow(new RuntimeException("Encryption error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bigDecimalEncryptor.convertToDatabaseColumn(attribute);
        });

        assertEquals("Error encrypting BigDecimal", exception.getMessage());
    }

    @Test
    void convertToEntityAttribute_ShouldDecryptString() {
        String dbData = "encryptedValue";
        encryptionUtilMock.when(() -> EncryptionUtil.decrypt(anyString(), anyString())).thenReturn("123.45");

        BigDecimal decryptedValue = bigDecimalEncryptor.convertToEntityAttribute(dbData);

        assertEquals(new BigDecimal("123.45"), decryptedValue);
        encryptionUtilMock.verify(() -> EncryptionUtil.decrypt("encryptedValue", "testKey"), Mockito.times(1));
    }

    @Test
    void convertToEntityAttribute_ShouldThrowRuntimeExceptionOnDecryptionError() {
        String dbData = "encryptedValue";
        encryptionUtilMock.when(() -> EncryptionUtil.decrypt(anyString(), anyString())).thenThrow(new RuntimeException("Decryption error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bigDecimalEncryptor.convertToEntityAttribute(dbData);
        });

        assertEquals("Error decrypting BigDecimal", exception.getMessage());
    }

    @Test
    void convertToDatabaseColumn_ShouldHandleNullAttribute() {
        String encryptedValue = bigDecimalEncryptor.convertToDatabaseColumn(null);

        assertNull(encryptedValue);
    }

    @Test
    void convertToEntityAttribute_ShouldHandleNullDbData() {
        BigDecimal decryptedValue = bigDecimalEncryptor.convertToEntityAttribute(null);

        assertNull(decryptedValue);
    }
}