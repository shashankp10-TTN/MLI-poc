package com.order_service_poc.consumer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service_poc.consumer.entity.EncryptionType;
import com.order_service_poc.consumer.entity.Keys;
import com.order_service_poc.consumer.repo.EncryptionTypeRepo;
import com.order_service_poc.consumer.repo.KeysRepo;
import com.order_service_poc.consumer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.management.InstanceNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
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
    public String generateExchangePublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        System.out.println("Public Key: " + publicKey);
        System.out.println("Private Key: " + privateKey);

        Map<String, String> map = new HashMap<>();
        map.put("publicKey", publicKey);
        map.put("privateKey", privateKey);

        keysRepo.save(Keys.builder()
                        .keys(map)
                        .build());
        return publicKey;
    }

    @Override
    public String storeClientSecret(String encryptedClientSecret) throws Exception {
        if(encryptedClientSecret==null || encryptedClientSecret.isEmpty()){
            throw new BadRequestException("client secret cannot be empty");
        }
        System.out.println("encrypted client secret: " + encryptedClientSecret);
        String originalClientSecret = decryptClientSecret(encryptedClientSecret);

        ObjectMapper objectMapper = new ObjectMapper();
        String originalEncodedPayload = objectMapper.readValue(originalClientSecret, String.class);
        System.out.println("client secret key: " + originalEncodedPayload);

        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", originalEncodedPayload);
        keysRepo.save(Keys.builder()
                .keys(map)
                .build());
        return "Client secret is stored successfully!";
    }

    @Override
    public String decryptClientSecret(String encryptedPayload) throws Exception {
        if(encryptedPayload==null || encryptedPayload.isEmpty()){
            throw new BadRequestException("Payload cannot be empty");
        }
        // 1. decrypt using private key
        List<Map<String, String>> keyMap = keysRepo.findAll()
                .stream()
                .map(Keys::getKeys)
                .toList();
        String privateKey = null;
        for(Map<String, String> key : keyMap){
            if(key.containsKey("privateKey")) {
                privateKey = key.get("privateKey");
            }
        }
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyFromBase64(privateKey));
        byte[] encryptedSecretBytes = Base64.getDecoder().decode(encryptedPayload);
        byte[] decryptedSecretBytes = cipher.doFinal(encryptedSecretBytes);

        return new String(decryptedSecretBytes);

    }

    @Override
    public String decryptPayload(String encryptedPayload) throws Exception {
        if(encryptedPayload==null || encryptedPayload.isEmpty()){
            throw new BadRequestException("Payload cannot be empty");
        }
        //  decrypt using secret key
        List<Map<String, String>> keyMap = keysRepo.findAll()
                .stream()
                .map(Keys::getKeys)
                .toList();
        String clientSecret = null;
        for(Map<String, String> key : keyMap){
            if(key.containsKey("clientSecret")) {
                clientSecret = key.get("clientSecret");
            }
        }
        List<EncryptionType> encryptionTypeList = encryptionTypeRepo.findAll();
        if(encryptionTypeList.isEmpty()){
            throw new InstanceNotFoundException("Encryption type not found");
        }
        boolean isAESEncrypted = encryptionTypeList.stream().findFirst().get().isAESEncrypted();
        byte[] decryptedSecretBytes = null;
        if(isAESEncrypted) {
            byte[] keyBytes = clientSecret.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            System.out.println("Decryption using AES");
            byte[] encryptedSecretBytes = Base64.getDecoder().decode(encryptedPayload);
            decryptedSecretBytes = cipher.doFinal(encryptedSecretBytes);
        } else {

         //  Resource : https://medium.com/@johnvazna/implementing-local-aes-gcm-encryption-and-decryption-in-java-ac1dacaaa409

            byte[] decodedCipherText = Base64.getDecoder().decode(encryptedPayload);

            // Extract IV and encrypted text
            byte[] iv = new byte[IV_LENGTH_ENCRYPT];
            System.arraycopy(decodedCipherText, 0, iv, 0, iv.length);
            byte[] encryptedText = new byte[decodedCipherText.length - IV_LENGTH_ENCRYPT];
            System.arraycopy(decodedCipherText, IV_LENGTH_ENCRYPT, encryptedText, 0, encryptedText.length);

            // Initialize cipher in AES-GCM mode
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_ENCRYPT * 8, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(clientSecret), gcmSpec);

            // Decrypt the ciphertext
            decryptedSecretBytes = cipher.doFinal(encryptedText);
        }

        return new String(decryptedSecretBytes);
    }

    private SecretKeySpec getSecretKeySpec(String clientSecret) {
        byte[] keyBytes = clientSecret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }

    private PrivateKey getPrivateKeyFromBase64(String base64PrivateKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }


}
