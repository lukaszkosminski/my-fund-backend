package com.myfund.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class DataEncryptionService {

    public String encryptData(String data, Long userId) throws Exception {
        String encryptionKey = generateEncryptionKey(userId);
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptData(String encryptedData, Long userId) throws Exception {
        String encryptionKey = generateEncryptionKey(userId);
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] original = cipher.doFinal(decoded);
        return new String(original, StandardCharsets.UTF_8);
    }

    private String generateEncryptionKey(Long userId) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update((userId.toString()).getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return Base64.getEncoder().encodeToString(digest).replaceAll("[^A-Za-z0-9]", "").substring(0, 16);
    }
}
