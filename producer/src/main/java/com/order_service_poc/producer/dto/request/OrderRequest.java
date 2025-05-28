package com.order_service_poc.producer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderRequest {
    private String id;
    @PositiveOrZero(message = "Quantity cannot be negative")
    @NotNull(message = "Quantity cannot be empty")
    private Integer quantity;
}
