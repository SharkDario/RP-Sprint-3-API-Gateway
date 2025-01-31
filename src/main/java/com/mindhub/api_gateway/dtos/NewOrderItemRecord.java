package com.mindhub.api_gateway.dtos;

public record NewOrderItemRecord(Long orderId, Long productId, Integer quantity) {
}