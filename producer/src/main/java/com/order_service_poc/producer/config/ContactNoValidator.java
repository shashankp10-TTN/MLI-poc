package com.order_service_poc.producer.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContactNoValidator implements ConstraintValidator<ValidContactNo, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && value.startsWith("+91-");
    }
}
