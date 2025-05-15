package com.order_service_poc.producer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Response {
    private Integer statusCode;
    private String message;
}
