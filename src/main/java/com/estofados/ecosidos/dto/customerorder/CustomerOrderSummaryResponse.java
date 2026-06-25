package com.estofados.ecosidos.dto.customerorder;

import java.time.LocalDate;

public record CustomerOrderSummaryResponse(
        Long id,
        String code,
        Long customerId,
        String customerCode,
        String customerName,
        String status,
        LocalDate orderDate
) {
}
