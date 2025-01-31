package com.mindhub.api_gateway.dtos;

import java.util.List;

public record NewOrderRecord(String email, List<ProductQuantityRecord> recordList) {
}
