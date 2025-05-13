package com.order_service_poc.consumer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private String id;
    private Integer quantity;
}
