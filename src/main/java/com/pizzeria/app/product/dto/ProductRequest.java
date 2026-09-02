package com.pizzeria.app.product.dto;

import com.pizzeria.app.product.entity.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        String description,

        @NotNull(message = "Product category is required")
        ProductCategory category,

        @NotNull(message = "Product price is required")
        @Positive(message = "Product price must be greater than zero")
        BigDecimal price,

        boolean available
) {
}
