package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;

public record SuppliedSemiFinishedOrderResult(
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        BigDecimal quantity) {
}
