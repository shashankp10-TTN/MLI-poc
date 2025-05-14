package com.order_service_poc.producer.service.impl;

import com.order_service_poc.producer.service.EncryptionService;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import java.io.*;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class EncryptionServiceImpl implements EncryptionService {

    @Override
    public String storeAsymmetricKey(String publicKey) {
        String fileName = "asymmetric_publicKey.txt";
        String resourcePath = "src/main/resources/" + fileName;
        try {
            File file = new File(resourcePath);
            FileWriter writer = new FileWriter(file);
            writer.write("publicKey" + "=" + publicKey + System.lineSeparator());
            writer.close();
            return "Public key successfully stored!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Something went wrong, unable to store public key!";
        }
    }

    @Override
    public String generateSymmetricKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();

        storeSymmetricKey(secretKey.toString());
        System.out.println("producer : " +  Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        return encryptSymmetricKey(secretKey.toString());
    }

    private String encryptSymmetricKey(String clientSecret) throws Exception {
        String publicAsymmetricKey = getKeyFromFile("publicKey","asymmetric_publicKey.txt");
//        String clientSecret = getKeyFromFile("secretKey", "symmetricKey.txt"); // data

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromBase64(publicAsymmetricKey));
        byte[] encryptedBytes = null;
        if(clientSecret!=null)
            encryptedBytes = cipher.doFinal(clientSecret.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private PublicKey getPublicKeyFromBase64(String base64PublicKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
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

    private void storeSymmetricKey(String key) {
        String fileName = "symmetricKey.txt";
        String resourcePath = "src/main/resources/" + fileName;
        try {
            File file = new File(resourcePath);
            FileWriter writer = new FileWriter(file);
            writer.write("secretKey" + "=" + key + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
