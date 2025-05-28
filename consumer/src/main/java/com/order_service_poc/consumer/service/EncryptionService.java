package com.order_service_poc.consumer.service;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Map;

public interface EncryptionService {

   String generateExchangePublicKey() throws NoSuchAlgorithmException;

   String decryptPayload(String encryptedPayload) throws Exception;

   String decryptClientSecret(String encryptedPayload) throws Exception;

   String storeClientSecret(String encryptedClientSecret) throws Exception;

}
