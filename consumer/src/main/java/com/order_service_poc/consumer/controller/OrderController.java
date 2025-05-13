package com.order_service_poc.consumer.controller;

import com.order_service_poc.consumer.dto.OrderRequest;
import com.order_service_poc.consumer.dto.OrderResponse;
import com.order_service_poc.consumer.dto.ProductRequest;
import com.order_service_poc.consumer.entity.Product;
import com.order_service_poc.consumer.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/consumer")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody ProductRequest productRequest) {
        String message = orderService.addProduct(productRequest);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        String message = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok(OrderResponse.builder().message(message).build());
    }
}
