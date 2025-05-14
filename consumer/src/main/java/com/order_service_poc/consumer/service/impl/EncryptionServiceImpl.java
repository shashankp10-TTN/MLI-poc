package com.order_service_poc.consumer.service.impl;

import com.order_service_poc.consumer.entity.AsymmetricKey;
import com.order_service_poc.consumer.entity.Keys;
import com.order_service_poc.consumer.repo.AsymmetricKeyRepo;
import com.order_service_poc.consumer.repo.KeysRepo;
import com.order_service_poc.consumer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EncryptionServiceImpl implements EncryptionService {

    private final AsymmetricKeyRepo asymmetricKeyRepo;
    private final KeysRepo keysRepo;

    @Override
    public String generateAsymmetricKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        Map<String, String> map = new HashMap<>();
        map.put("publicKey", publicKey);
        map.put("privateKey", privateKey);

        asymmetricKeyRepo.save(AsymmetricKey.builder()
                                .keys(map)
                                .build());
        return publicKey;
    }

    @Override
    public String storeSymmetricKey(String data) throws Exception {
        List<Map<String, String>> keyMap = asymmetricKeyRepo.findAll()
                .stream()
                .map(AsymmetricKey::getKeys)
                .toList();
        String privateAsymmetricKey = null;
        for(Map<String, String> key : keyMap){
            if(key.containsKey("privateKey")) {
                privateAsymmetricKey = key.get("privateKey");
            }
        }

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyFromBase64(privateAsymmetricKey));

        byte[] encryptedSecretBytes = Base64.getDecoder().decode(data);
        byte[] decryptedSecretBytes = cipher.doFinal(encryptedSecretBytes);
        String clientSecretKey = new String(decryptedSecretBytes, StandardCharsets.UTF_8);
        System.out.println("consumer side, client key: " + clientSecretKey);

        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", clientSecretKey);
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
