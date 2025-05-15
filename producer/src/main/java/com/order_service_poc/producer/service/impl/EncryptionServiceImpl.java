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
        return encryptClientSecretKey(clientSecretKey);
    }

    private String encryptClientSecretKey(String clientSecret) throws Exception {
        List<Map<String, String>> keyMap = keysRepo.findAll()
                .stream()
                .map(Keys::getKeys)
                .toList();
        String exchangeKey = null;
        for (Map<String, String> key : keyMap) {
            if (key.containsKey("publicKey")) {
                exchangeKey = key.get("publicKey");
            }
        }
        // 1. convert data to JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(clientSecret);

        // 2. encrypt the data using exchange key
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromBase64(exchangeKey));
        byte[] encryptedBytes = null;
        if (clientSecret != null)
            encryptedBytes = cipher.doFinal(jsonData.getBytes());

        // 3. convert in Base64 encode for safe transfer
        String encodedEncryptedClientSecret = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("client secret key after encryption : " + encodedEncryptedClientSecret);
        return encodedEncryptedClientSecret;
    }

    private PublicKey getPublicKeyFromBase64(String base64PublicKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }


}
