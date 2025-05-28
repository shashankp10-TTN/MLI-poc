package com.order_service_poc.producer.config;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = ContactNoValidator.class)
public @interface ValidContactNo {
    String message() default "Contact number must be Indian";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
