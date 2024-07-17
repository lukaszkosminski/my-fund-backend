package com.myfund.services.encryption;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;

@Converter
public class LocalDateTimeEncryptor implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${encryption.key}")
    private String encryptionKey;

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String formattedDateTime = attribute.format(FORMATTER);
            return EncryptionUtil.encrypt(formattedDateTime, encryptionKey);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting LocalDateTime", e);
        }
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            String decryptedData = EncryptionUtil.decrypt(dbData, encryptionKey);
            return LocalDateTime.parse(decryptedData, FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting LocalDateTime", e);
        }
    }
}