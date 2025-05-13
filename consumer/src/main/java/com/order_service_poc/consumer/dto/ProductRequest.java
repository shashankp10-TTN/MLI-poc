package com.order_service_poc.consumer.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ProductRequest {
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer quantity;
}
