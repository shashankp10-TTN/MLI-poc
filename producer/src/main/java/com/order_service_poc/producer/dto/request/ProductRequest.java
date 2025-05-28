package com.order_service_poc.producer.dto.request;

import com.order_service_poc.producer.config.ValidContactNo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 30)
    private String productName;

    @NotBlank(message = "Description cannot be be empty")
    @Size(max = 100)
    private String description;

    @ValidContactNo
    @NotBlank(message = "Contact No. cannot be blank")
    private String contactNo;

    @PositiveOrZero(message = "Price cannot be negative")
    @NotNull(message = "Price cannot be empty")
    private BigDecimal price;

    @PositiveOrZero(message = "Quantity cannot be negative")
    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;
}
