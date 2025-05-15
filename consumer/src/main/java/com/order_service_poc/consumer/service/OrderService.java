package com.order_service_poc.consumer.service;

import com.order_service_poc.consumer.dto.OrderRequest;
import com.order_service_poc.consumer.dto.ProductRequest;
import com.order_service_poc.consumer.entity.Product;

public interface OrderService {

    String addProduct(String productRequest) throws Exception;

    String placeOrder(String orderRequest) throws Exception;
}
