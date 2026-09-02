package com.pizzeria.app.order.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusUpdateRequest(

        @NotBlank(message = "Field 'status' is required")
        String status
) {
}
