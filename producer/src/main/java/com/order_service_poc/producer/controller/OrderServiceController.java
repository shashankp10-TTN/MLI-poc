package com.order_service_poc.producer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service_poc.producer.dto.request.OrderRequest;
import com.order_service_poc.producer.dto.request.ProductRequest;
import com.order_service_poc.producer.dto.response.Response;
import com.order_service_poc.producer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/producer")
public class OrderServiceController {

    private final RestTemplate restTemplate;
    private final EncryptionService encryptionService;

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody ProductRequest productRequest) throws Exception {
        try {
            String url = "http://localhost:8082/api/v1/consumer/add";

            System.out.println("Actual payload : " + productRequest.getProductName() + ", " + productRequest.getPrice());
            ObjectMapper objectMapper = new ObjectMapper();
            String encodedJson = objectMapper.writeValueAsString(productRequest);
            System.out.println("Encoded json : " + encodedJson);

            String encryptedPayload = encryptionService.encryptPayload(encodedJson);
            System.out.println("Encrypted payload : " + encryptedPayload);
            restTemplate.postForEntity(url, encryptedPayload, String.class);
            return ResponseEntity.ok("Product added successfully");
        } catch (HttpClientErrorException ex) {
            Response orderResponse = Response.builder()
                    .statusCode(ex.getStatusCode().value())
                    .message(ex.getResponseBodyAsString())
                    .build();
            return ResponseEntity.badRequest().body("Something went wrong!");
        }
    }

    @PostMapping("/order")
    public ResponseEntity<Response> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            String url = "http://localhost:8082/api/v1/consumer/order";

            System.out.println("Actual payload : " + orderRequest.getId() + ", " + orderRequest.getQuantity());
            ObjectMapper objectMapper = new ObjectMapper();
            String encodedJson = objectMapper.writeValueAsString(orderRequest);
            System.out.println("Encoded json : " + encodedJson);

            String encryptedPayload = encryptionService.encryptPayload(encodedJson);
            System.out.println("Encrypted payload : " + encryptedPayload);

            restTemplate.postForEntity(url, encryptedPayload, String.class);
            Response orderResponse = Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Order placed successfully!")
                    .build();
            return ResponseEntity.ok(orderResponse);
        } catch (HttpClientErrorException ex) {
            Response orderResponse = Response.builder()
                    .statusCode(ex.getStatusCode().value())
                    .message(ex.getResponseBodyAsString())
                    .build();
            return ResponseEntity.badRequest().body(orderResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public ResponseEntity<String> sendClientSecret() {
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
