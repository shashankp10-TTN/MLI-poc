package com.order_service_poc.consumer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service_poc.consumer.entity.Keys;
import com.order_service_poc.consumer.repo.KeysRepo;
import com.order_service_poc.consumer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
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

        // 1. decode from base64
        byte[] keyBytes = Base64.getDecoder().decode(encryptedClientSecret);

        // 2. decrypt using private key
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
        byte[] encryptedSecretBytes = Base64.getDecoder().decode(encryptedClientSecret);
        byte[] decryptedSecretBytes = cipher.doFinal(encryptedSecretBytes);

        // 3. convert back to original form

        String decryptedClientSecret = new String(decryptedSecretBytes);
        ObjectMapper objectMapper = new ObjectMapper();
        String originalClientSecret = objectMapper.readValue(decryptedClientSecret, String.class);
        System.out.println("client secret key: " + originalClientSecret);

        // 4. store client secret
        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", originalClientSecret);
        keysRepo.save(Keys.builder()
                .keys(map)
                .build());
        return "Client secret is stored successfully!";
    }

    private PrivateKey getPrivateKeyFromBase64(String base64PrivateKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

}
