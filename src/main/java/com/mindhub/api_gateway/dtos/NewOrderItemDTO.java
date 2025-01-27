package com.mindhub.api_gateway.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NewOrderItemDTO(
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be a positive number")
        Integer quantity) {
}
