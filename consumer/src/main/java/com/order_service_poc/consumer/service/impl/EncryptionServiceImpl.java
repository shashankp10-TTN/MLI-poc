package com.order_service_poc.consumer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service_poc.consumer.entity.Keys;
import com.order_service_poc.consumer.repo.KeysRepo;
import com.order_service_poc.consumer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
        byte[] keyBytes = clientSecret.getBytes("UTF-8");
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedSecretBytes = Base64.getDecoder().decode(encryptedPayload);
        byte[] decryptedSecretBytes = cipher.doFinal(encryptedSecretBytes);

        return new String(decryptedSecretBytes);
    }

    private PrivateKey getPrivateKeyFromBase64(String base64PrivateKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

//    private SecretKey getClientSecretFromBase64(String base64ClientSecretKey)  {
//        byte[] decodedKey = Base64.getDecoder().decode(base64ClientSecretKey);
//        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//    }

}
