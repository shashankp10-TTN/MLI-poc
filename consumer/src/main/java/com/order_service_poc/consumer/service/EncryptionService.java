package com.order_service_poc.consumer.service;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Map;

public interface EncryptionService {

   PublicKey generateAsymmetricKeys() throws NoSuchAlgorithmException;

   String storeSymmetricKey(String secretKey) throws Exception;
}
