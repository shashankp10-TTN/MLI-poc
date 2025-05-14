package com.order_service_poc.producer.repo;

import com.order_service_poc.producer.entity.Keys;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.security.Key;
import java.util.Map;

public interface AsymmetricKeyRepo extends MongoRepository<Keys, String> {
    String findByKeys(Map<String, Key> keys);

    String findAllByKeysContaining(Map<String, Key> keys);
}
