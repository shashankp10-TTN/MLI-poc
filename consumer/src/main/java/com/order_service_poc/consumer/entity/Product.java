package com.order_service_poc.consumer.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Builder
@Document(collection = "product")
@Getter
@Setter
public class Product {
    @Id
    private String id;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer quantity;

}
