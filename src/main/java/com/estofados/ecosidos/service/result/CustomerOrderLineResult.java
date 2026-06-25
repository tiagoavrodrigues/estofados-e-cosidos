package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;

public record CustomerOrderLineResult(
        Long id,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity
) {
}
