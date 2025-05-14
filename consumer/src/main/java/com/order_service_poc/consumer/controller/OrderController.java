package com.order_service_poc.consumer.controller;

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
    public ResponseEntity<String> addProduct(@RequestBody ProductRequest productRequest) {
        String message = orderService.addProduct(productRequest);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        String message = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok(OrderResponse.builder().message(message).build());
    }

    @GetMapping("/public-key")
    public ResponseEntity<PublicKey> sendAsymmetricPublicKey() throws NoSuchAlgorithmException {
        PublicKey publicKey = encryptionService.generateAsymmetricKeys();
        return ResponseEntity.ok(publicKey);
    }

    @PostMapping("/data-key")
    public ResponseEntity<String> generateAsymmetricKey(@RequestBody String symmetricKey) throws Exception {
        String message = encryptionService.storeSymmetricKey(symmetricKey);
        return ResponseEntity.ok(message);
    }
}
