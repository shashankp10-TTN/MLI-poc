package com.order_service_poc.producer.service;

public interface EncryptionService {

    String storeExchangePublicKey(String publicKey) throws Exception;

    String generateClientSecret() throws Exception;

    String encryptPayload(String payload) throws Exception;

    String setEncryptionType(Boolean isAESEncrypted);

}

