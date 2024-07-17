package com.myfund.services.encryption;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private static final String SECRET_KEY = "1234567890123456";
    private static final String ORIGINAL_TEXT = "Hello, World!";

    @Test
    void testEncrypt_ShouldReturnNonEmptyString() throws Exception {
        String encryptedText = EncryptionUtil.encrypt(ORIGINAL_TEXT, SECRET_KEY);
        assertNotNull(encryptedText, "Encrypted text should not be null");
        assertFalse(encryptedText.isEmpty(), "Encrypted text should not be empty");
    }

    @Test
    void testDecrypt_ShouldReturnOriginalText() throws Exception {
        String encryptedText = EncryptionUtil.encrypt(ORIGINAL_TEXT, SECRET_KEY);
        String decryptedText = EncryptionUtil.decrypt(encryptedText, SECRET_KEY);
        assertEquals(ORIGINAL_TEXT, decryptedText, "Decrypted text should match the original text");
    }

    @Test
    void testEncryptAndDecrypt_ShouldHandleEmptyString() throws Exception {
        String encryptedText = EncryptionUtil.encrypt("", SECRET_KEY);
        String decryptedText = EncryptionUtil.decrypt(encryptedText, SECRET_KEY);
        assertEquals("", decryptedText, "Decrypted text should be an empty string");
    }
}