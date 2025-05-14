package com.order_service_poc.producer.service.impl;

import com.order_service_poc.producer.entity.Keys;
import com.order_service_poc.producer.repo.AsymmetricKeyRepo;
import com.order_service_poc.producer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EncryptionServiceImpl implements EncryptionService {

    private final AsymmetricKeyRepo asymmetricKeyRepo;

    @Override
    public String storeAsymmetricKey(String publicKey) throws Exception {
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("publicKey", publicKey);
        asymmetricKeyRepo.save(Keys.builder()
                                    .keys(keyMap)
                                    .build());
        return "Asymmetric public key stored!";
    }

    @Override
    public String generateSymmetricKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();

        String clientSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("Before encryption: " + clientSecretKey);

        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("clientSecret", clientSecretKey);
        asymmetricKeyRepo.save(Keys.builder()
                .keys(keyMap)
                .build());
        return encryptClientSecretKey(secretKey);
    }

    private String encryptClientSecretKey(Key clientSecret) throws Exception {
        List<Map<String, String>> keyMap = asymmetricKeyRepo.findAll()
                                            .stream()
                                            .map(Keys::getKeys)
                                            .toList();
        String publicAsymmetricKey = null;
        for(Map<String, String> key : keyMap){
            if(key.containsKey("publicKey")) {
                publicAsymmetricKey = key.get("publicKey");
            }
        }

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromBase64(publicAsymmetricKey));
        byte[] encryptedBytes = null;
        if(clientSecret!=null)
            encryptedBytes = cipher.doFinal(clientSecret.getEncoded());
        System.out.println("After encrypting client secret : " + Base64.getEncoder().encodeToString(encryptedBytes));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private PublicKey getPublicKeyFromBase64(String base64PublicKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }


}
