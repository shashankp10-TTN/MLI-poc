package com.order_service_poc.producer.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.security.Key;
import java.util.Map;

@Builder
@Document(collection = "clientAsymmetricKey")
@Getter
@Setter
public class Keys {
    @Id
    private String id;
    private Map<String, String> keys;
}
