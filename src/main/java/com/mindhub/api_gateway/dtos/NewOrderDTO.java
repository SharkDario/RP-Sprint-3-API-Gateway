package com.mindhub.api_gateway.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NewOrderDTO(
        @NotBlank(message = "Id is required")
        Long id,

        @NotBlank(message = "User is required")
        Long userId,

        List<NewOrderItemDTO> products,
        @NotNull(message = "Order status is required and cannot be null")
        OrderStatus status) {
}
