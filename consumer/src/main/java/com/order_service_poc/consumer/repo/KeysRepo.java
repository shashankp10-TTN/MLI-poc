package com.order_service_poc.consumer.repo;

import com.order_service_poc.consumer.entity.Keys;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KeysRepo extends MongoRepository<Keys, String> {
}
