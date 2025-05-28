package com.order_service_poc.producer.controller;

import com.order_service_poc.producer.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/encryption")
public class EncryptionTypeController {

    private final EncryptionService encryptionService;

    @PostMapping("/update")
    public ResponseEntity<String> setEncryptionType(@RequestParam Boolean encryptionType) {
        String response = encryptionService.setEncryptionType(encryptionType);
        return ResponseEntity.ok(response);
    }
}
