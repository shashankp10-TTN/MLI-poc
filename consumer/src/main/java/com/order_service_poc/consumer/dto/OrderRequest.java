package com.order_service_poc.consumer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private String id;
    @PositiveOrZero(message = "Quantity cannot be negative")
    @NotBlank(message = "Quantity cannot be empty")
    private Integer quantity;
}
