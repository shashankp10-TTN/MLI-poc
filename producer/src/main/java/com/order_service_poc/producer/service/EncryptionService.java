package com.order_service_poc.producer.service;

public interface EncryptionService {

    String storeAsymmetricKey(String publicKey) throws Exception;

    String generateSymmetricKey() throws Exception;

//    String encryptSymmetricKey(String clientSecret) throws Exception;

}

