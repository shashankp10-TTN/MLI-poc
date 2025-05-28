package com.order_service_poc.consumer.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document(collection = "encryption")
@Getter
@Setter
public class EncryptionType {
    @Id
    private String id;
    private boolean isAESEncrypted;
}
