package com.order_service_poc.consumer.repo;

import com.order_service_poc.consumer.entity.EncryptionType;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EncryptionTypeRepo extends MongoRepository<EncryptionType, String> {
}
