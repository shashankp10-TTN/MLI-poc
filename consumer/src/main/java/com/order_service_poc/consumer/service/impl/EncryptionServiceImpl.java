package com.order_service_poc.consumer.service.impl;

import com.order_service_poc.consumer.entity.AsymmetricKey;
import com.order_service_poc.consumer.repo.AsymmetricKeyRepo;
import com.order_service_poc.consumer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EncryptionServiceImpl implements EncryptionService {

    private final AsymmetricKeyRepo asymmetricKeyRepo;

    @Override
    public PublicKey generateAsymmetricKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Map<String, Key> map = new HashMap<>();
        map.put("publicKey", keyPair.getPublic());
        map.put("privateKey", keyPair.getPrivate());

        asymmetricKeyRepo.save(AsymmetricKey.builder()
                                .keys(map)
                                .build());
        return keyPair.getPublic();
    }

    @Override
    public String storeSymmetricKey(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        String privateKey = getKeyFromFile("privateKey", "asymmetric_keys.txt");

        cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyFromBase64(privateKey));

        byte[] encryptedBytes = Base64.getDecoder().decode(data);
        byte[] decryptedMessageBytes = cipher.doFinal(encryptedBytes);
        String secretKey = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        String fileName = "client_secretKey.txt";
        String resourcePath = "src/main/resources/" + fileName;
        try {
            File file = new File(resourcePath);
            FileWriter writer = new FileWriter(file);
            writer.write("clientSecret=" + secretKey);
            writer.close();
            return "Key stored successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "Something went wrong! Unable to store the key";
        }
    }

    private PrivateKey getPrivateKeyFromBase64(String base64PrivateKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private String getKeyFromFile(String key, String fileName) {
        String resourcePath = "src/main/resources/" + fileName;
        File file = new File(resourcePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(key+"=")) {
                    return line.substring((key + "=").length()).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
