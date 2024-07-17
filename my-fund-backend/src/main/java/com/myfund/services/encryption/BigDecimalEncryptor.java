package com.myfund.services.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

@Converter
public class BigDecimalEncryptor implements AttributeConverter<BigDecimal, String> {

    @Value("${encryption.key}")
    private String encryptionKey;

    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return EncryptionUtil.encrypt(attribute.toString(), encryptionKey);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting BigDecimal", e);
        }
    }

    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return new BigDecimal(EncryptionUtil.decrypt(dbData, encryptionKey));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting BigDecimal", e);
        }
    }
}