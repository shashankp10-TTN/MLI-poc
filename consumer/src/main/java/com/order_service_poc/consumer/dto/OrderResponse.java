package com.order_service_poc.consumer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class OrderResponse {
    private Integer statusCode;
    private String message;
}
