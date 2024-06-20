package com.myfund.services.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Converter
public class StringEncryptor implements AttributeConverter<String, String> {

    @Value("${encryption.key}")
    private String encryptionKey;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            return EncryptionUtil.encrypt(attribute, encryptionKey);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting string", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            return EncryptionUtil.decrypt(dbData, encryptionKey);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting string", e);
        }
    }
}
