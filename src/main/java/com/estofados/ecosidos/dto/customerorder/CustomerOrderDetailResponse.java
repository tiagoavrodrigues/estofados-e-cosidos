package com.estofados.ecosidos.dto.customerorder;

import java.time.LocalDate;
import java.util.List;

public record CustomerOrderDetailResponse(
        Long id,
        String code,
        Long customerId,
        String customerCode,
        String customerName,
        String status,
        LocalDate orderDate,
        String notes,
        List<CustomerOrderLineResponse> lines
) {
}
