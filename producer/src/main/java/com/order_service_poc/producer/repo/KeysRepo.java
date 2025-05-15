package com.order_service_poc.producer.repo;

import com.order_service_poc.producer.entity.Keys;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KeysRepo extends MongoRepository<Keys, String> {

}
