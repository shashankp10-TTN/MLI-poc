package com.order_service_poc.producer.repo;

import com.order_service_poc.producer.entity.EncryptionType;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EncryptionTypeRepo extends MongoRepository<EncryptionType, String> {
}
