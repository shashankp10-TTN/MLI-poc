package com.order_service_poc.producer.service;

public interface EncryptionService {

    String storeExchangePublicKey(String publicKey) throws Exception;

    String generateClientSecret() throws Exception;

//    String encryptSymmetricKey(String clientSecret) throws Exception;

}

