package com.order_service_poc.producer.service;

public interface EncryptionService {

    String storeAsymmetricKey(String publicKey);

    String generateSymmetricKey() throws Exception;

//    String encryptSymmetricKey(String clientSecret) throws Exception;

}

