package com.order_service_poc.consumer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service_poc.consumer.dto.OrderRequest;
import com.order_service_poc.consumer.dto.OrderResponse;
import com.order_service_poc.consumer.dto.ProductRequest;
import com.order_service_poc.consumer.entity.Product;
import com.order_service_poc.consumer.service.EncryptionService;
import com.order_service_poc.consumer.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/consumer")
public class OrderController {

    private final OrderService orderService;
    private final EncryptionService encryptionService;

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody String encryptedPayload) throws Exception {
        String message = orderService.addProduct(encryptedPayload);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        String message = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok(OrderResponse.builder().message(message).build());
    }

    @GetMapping("/public-key")
    public ResponseEntity<String> sendExchangePublicKey() throws NoSuchAlgorithmException {
        String exchangePublicKey = encryptionService.generateExchangePublicKey();
        return ResponseEntity.ok(exchangePublicKey);
    }

    @PostMapping("/data-key")
    public ResponseEntity<String> generateAsymmetricKey(@RequestBody String encryptedClientSecret) throws Exception {
        String message = encryptionService.storeClientSecret(encryptedClientSecret);
        return ResponseEntity.ok(message);
    }
}
