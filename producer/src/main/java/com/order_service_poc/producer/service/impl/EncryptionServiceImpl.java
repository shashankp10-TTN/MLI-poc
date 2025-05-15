package com.order_service_poc.producer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service_poc.producer.entity.Keys;
import com.order_service_poc.producer.repo.KeysRepo;
import com.order_service_poc.producer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EncryptionServiceImpl implements EncryptionService {

    private final KeysRepo keysRepo;

    @Override
    public String storeExchangePublicKey(String publicKey) throws Exception {
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("publicKey", publicKey);
        System.out.println("Public Key: " + publicKey);
        keysRepo.save(Keys.builder()
                .keys(keyMap)
                .build());
        return "Exchange public key stored!";
    }

    @Override
    public String generateClientSecret() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();

        String clientSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("client secret key before encryption: " + clientSecretKey);

        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("clientSecret", clientSecretKey);
        keysRepo.save(Keys.builder()
                .keys(keyMap)
                .build());

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(clientSecretKey);

        return encryptClientSecret(jsonData);
    }

    public String encryptClientSecret(String jsonData) throws Exception {
        // extract exchange key from the db
        List<Map<String, String>> keyMap = keysRepo.findAll()
                .stream()
                .map(Keys::getKeys)
                .toList();
        String clientSecret = null;
        for (Map<String, String> key : keyMap) {
            if (key.containsKey("publicKey")) {
                clientSecret = key.get("publicKey");
            }
        }

        // encrypt the data using exchange key
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromBase64(clientSecret));
        byte[] encryptedBytes = cipher.doFinal(jsonData.getBytes());

        // convert in Base64 encode for safe transfer
        System.out.println("Encrypted ClientSecret: " + Base64.getEncoder().encodeToString(encryptedBytes));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Override
    public String encryptPayload(String jsonData) throws Exception {
        // extract client secret key from the db
        List<Map<String, String>> keyMap = keysRepo.findAll()
                .stream()
                .map(Keys::getKeys)
                .toList();
        String clientSecret = null;
        for (Map<String, String> key : keyMap) {
            if (key.containsKey("clientSecret")) {
                clientSecret = key.get("clientSecret");
            }
        }

        // encrypt the data using exchange key
        byte[] keyBytes = clientSecret.getBytes("UTF-8");
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(jsonData.getBytes());

        // convert in Base64 encode for safe transfer
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

//    private SecretKey getClientSecretFromBase64(String base64ClientSecretKey) {
//        byte[] decodedKey = Base64.getDecoder().decode(base64ClientSecretKey);
//        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//    }

    private PublicKey getPublicKeyFromBase64(String base64PublicKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }


}
