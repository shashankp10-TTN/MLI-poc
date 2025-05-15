package com.order_service_poc.producer.controller;

import com.order_service_poc.producer.dto.request.OrderRequest;
import com.order_service_poc.producer.dto.request.ProductRequest;
import com.order_service_poc.producer.dto.response.OrderResponse;
import com.order_service_poc.producer.service.impl.EncryptionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.PublicKey;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/producer")
public class OrderServiceController {

    private final RestTemplate restTemplate;
    private final EncryptionServiceImpl encryptionService;

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody ProductRequest productRequest) {
        String url = "http://localhost:8082/api/v1/consumer/add";
        restTemplate.postForEntity(url, productRequest, String.class);
        return ResponseEntity.ok("Product added successfully");
    }

    @PostMapping("/order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            String url = "http://localhost:8082/api/v1/consumer/order";
            restTemplate.postForEntity(url, orderRequest, String.class);
            OrderResponse orderResponse = OrderResponse.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Product added successfully!")
                    .build();
            return ResponseEntity.ok(orderResponse);
        } catch (HttpClientErrorException ex) {
            OrderResponse orderResponse = OrderResponse.builder()
                    .statusCode(ex.getStatusCode().value())
                    .message(ex.getResponseBodyAsString())
                    .build();
            return ResponseEntity.badRequest().body(orderResponse);
        }
    }

    @GetMapping("/public-key")
    public ResponseEntity<String> getExchangePublicKey() {
        try {
            String url = "http://localhost:8082/api/v1/consumer/public-key";
            ResponseEntity<String> response =  restTemplate.getForEntity(url, String.class);
            String publicKey = response.getBody();
            return ResponseEntity.ok(encryptionService.storeExchangePublicKey(publicKey));
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/data-key")
    public ResponseEntity<String> sendSymmetricKey() {
        try {
            String url = "http://localhost:8082/api/v1/consumer/data-key";
            String encryptedSymmetricKey = encryptionService.generateClientSecret();
            restTemplate.postForEntity(url,encryptedSymmetricKey, String.class);
            return ResponseEntity.ok("Client secret generated successfully!");
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
