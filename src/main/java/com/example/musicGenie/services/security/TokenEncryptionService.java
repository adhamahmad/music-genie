package com.example.musicGenie.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
//@RequiredArgsConstructor
public class TokenEncryptionService {

    private final TextEncryptor encryptor;

    public TokenEncryptionService(
            @Value("${encryption.password}") String password,
            @Value("${encryption.salt}") String salt) {
        this.encryptor = Encryptors.text(password, salt);
    }

    public String encrypt(String plainText) {
        return encryptor.encrypt(plainText);
    }

    public String decrypt(String cipherText) {
        return encryptor.decrypt(cipherText);
    }
}