package com.order_service_poc.consumer.repo;

import com.order_service_poc.consumer.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepo extends MongoRepository<Product, String> {
}
