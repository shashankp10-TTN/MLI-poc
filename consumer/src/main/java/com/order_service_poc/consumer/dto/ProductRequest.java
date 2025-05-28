package com.order_service_poc.consumer.dto;

import com.order_service_poc.consumer.config.ValidContactNo;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;

@Setter
@Getter
public class ProductRequest {
    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 30)
    private String productName;

    @NotBlank(message = "Description cannot be be empty")
    @Size(max = 100)
    private String description;

    @PositiveOrZero(message = "Price cannot be negative")
    @NotBlank(message = "Price cannot be empty")
    private BigDecimal price;

    @PositiveOrZero(message = "Quantity cannot be negative")
    @NotNull(message = "Quantity cannot be empty")
    private Integer quantity;

    @ValidContactNo
    @NotBlank(message = "Contact No. cannot be blank")
    private String contactNo;
}
