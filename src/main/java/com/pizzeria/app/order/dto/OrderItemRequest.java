package com.pizzeria.app.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(

        @NotNull(message = "Each item needs a valid productId")
        Long productId,

        @Positive(message = "Each item needs a quantity greater than zero")
        int quantity
) {
}
