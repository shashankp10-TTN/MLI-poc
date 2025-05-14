package com.order_service_poc.consumer.repo;

import com.order_service_poc.consumer.entity.AsymmetricKey;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AsymmetricKeyRepo extends MongoRepository<AsymmetricKey, String> {
}
