package com.estofados.ecosidos.dto.customerorder;

import java.time.LocalDate;

public record CustomerOrderResponse(
        Long id,
        String code,
        Long customerId,
        String status,
        LocalDate orderDate,
        String notes
) {
}
