package com.order_service_poc.producer.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderRequest {
    private String id;
    private Integer quantity;
}
