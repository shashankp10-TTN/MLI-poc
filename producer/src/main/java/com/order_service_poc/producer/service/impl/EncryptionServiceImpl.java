package com.order_service_poc.producer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service_poc.producer.entity.EncryptionType;
import com.order_service_poc.producer.entity.Keys;
import com.order_service_poc.producer.repo.EncryptionTypeRepo;
import com.order_service_poc.producer.repo.KeysRepo;
import com.order_service_poc.producer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.management.InstanceNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EncryptionServiceImpl implements EncryptionService {

    private final KeysRepo keysRepo;
    private final EncryptionTypeRepo encryptionTypeRepo;

    public static final Integer IV_LENGTH_ENCRYPT = 12;
    public static final Integer TAG_LENGTH_ENCRYPT = 16;

    @Override
    public String storeExchangePublicKey(String publicKey) throws Exception {
        if(publicKey==null || publicKey.isEmpty()){
            throw new BadRequestException("public key cannot be empty");
        }
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
        if(jsonData==null || jsonData.isEmpty()){
            throw new BadRequestException("Data cannot be empty");
        }
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
        if(jsonData==null || jsonData.isEmpty()){
            throw new BadRequestException("Data cannot be empty");
        }
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

        List<EncryptionType> encryptionTypeList = encryptionTypeRepo.findAll();
        if(encryptionTypeList.isEmpty()){
            throw new InstanceNotFoundException("Encryption type not found");
        }

        boolean isAESEncrypted = encryptionTypeList.stream().findFirst().get().isAESEncrypted();


        if(isAESEncrypted){
            // encrypt the data using exchange key
            byte[] keyBytes = clientSecret.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            System.out.println("Encryption using AES");
            byte[] encryptedBytes = cipher.doFinal(jsonData.getBytes());
            // convert in Base64 encode for safe transfer
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } else {
            // encrypt the data using exchange key
            /*
            byte[] keyBytes = clientSecret.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES/GCM/NoPadding");

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            System.out.println("Encryption using AES/GCM/NoPadding");
            encryptedBytes = cipher.doFinal(jsonData.getBytes());

             */
            // Generate a random IV
            byte[] iv = new byte[IV_LENGTH_ENCRYPT];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            // Initialize cipher in AES-GCM mode
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_ENCRYPT * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(clientSecret), gcmSpec);

            // Encrypt the plaintext
            byte[] encryptedBytes = cipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted text and encode them as Base64
            byte[] combinedIvAndCipherText = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combinedIvAndCipherText, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combinedIvAndCipherText, iv.length, encryptedBytes.length);
            System.out.println("Encryption using AES/GCM/NoPadding");
            return Base64.getEncoder().encodeToString(combinedIvAndCipherText);
        }
    }
    private SecretKeySpec getSecretKeySpec(String clientSecret) {
        byte[] keyBytes = clientSecret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }
    @Override
    public String setEncryptionType(Boolean isAESEncrypted) {
        if(isAESEncrypted==null)
            throw new RuntimeException("Encryption type cannot be null");

        List<EncryptionType> encryptionTypeList = encryptionTypeRepo.findAll();
        EncryptionType encryptionType = null;
        if(encryptionTypeList.isEmpty()) {
            encryptionType = EncryptionType.builder()
                    .isAESEncrypted(isAESEncrypted)
                    .build();
            encryptionTypeList.add(encryptionType);
        } else {
            encryptionTypeList.stream().findFirst().get().setAESEncrypted(isAESEncrypted);
        }
        encryptionTypeRepo.saveAll(encryptionTypeList);
        return "Status is updated to " + isAESEncrypted + " successfully!";
    }


    private PublicKey getPublicKeyFromBase64(String base64PublicKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }


}
