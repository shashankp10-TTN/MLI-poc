package com.order_service_poc.consumer.service.impl;

import com.order_service_poc.consumer.dto.OrderRequest;
import com.order_service_poc.consumer.dto.ProductRequest;
import com.order_service_poc.consumer.entity.Product;
import com.order_service_poc.consumer.exception.OutOfStockException;
import com.order_service_poc.consumer.repo.ProductRepo;
import com.order_service_poc.consumer.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductRepo productRepo;

    @Override
    public String addProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .productName(productRequest.getProductName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();
        productRepo.save(product);
        return "Product added successfully";
    }

    @Override
    public String placeOrder(OrderRequest orderRequest) {
        Product product = productRepo.findById(orderRequest.getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if(product.getQuantity() < orderRequest.getQuantity()){
            throw new OutOfStockException("Product is out of stock!");
        }

        Integer newQuantity = product.getQuantity() - orderRequest.getQuantity();
        product.setQuantity(newQuantity);
        productRepo.save(product);
        return "Order placed successfully!!";
    }
}
