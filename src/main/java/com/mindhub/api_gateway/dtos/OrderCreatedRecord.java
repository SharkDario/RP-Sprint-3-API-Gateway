package com.mindhub.api_gateway.dtos;

import java.util.List;

public record OrderCreatedRecord(NewOrderDTO orderDTO, List<ErrorProductRecord> errorProductRecordList) {
}
