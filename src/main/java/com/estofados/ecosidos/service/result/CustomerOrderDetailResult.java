package com.estofados.ecosidos.service.result;

import java.time.LocalDate;
import java.util.List;

public record CustomerOrderDetailResult(
        Long id,
        String code,
        Long customerId,
        String customerCode,
        String customerName,
        String status,
        LocalDate orderDate,
        String notes,
        List<CustomerOrderLineResult> lines
) {
}
