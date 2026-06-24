package com.estofados.ecosidos.service.result;

import java.time.LocalDate;

public record CustomerOrderCreateResult(
        Long id,
        String code,
        Long customerId,
        String status,
        LocalDate orderDate,
        String notes
) {
}
