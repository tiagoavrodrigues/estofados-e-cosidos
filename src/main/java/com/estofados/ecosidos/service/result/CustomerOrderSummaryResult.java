package com.estofados.ecosidos.service.result;

import java.time.LocalDate;

public record CustomerOrderSummaryResult(
        Long id,
        String code,
        Long customerId,
        String customerCode,
        String customerName,
        String status,
        LocalDate orderDate
) {
}
