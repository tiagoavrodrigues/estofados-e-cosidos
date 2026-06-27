package com.estofados.ecosidos.dto.manufacturingorder;

import java.math.BigDecimal;

public record SuppliedSemiFinishedOrderResponse(
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        BigDecimal quantity) {
}
