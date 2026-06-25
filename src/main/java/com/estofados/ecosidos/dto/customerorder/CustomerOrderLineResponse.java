package com.estofados.ecosidos.dto.customerorder;

import java.math.BigDecimal;

public record CustomerOrderLineResponse(
        Long id,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity
) {
}
