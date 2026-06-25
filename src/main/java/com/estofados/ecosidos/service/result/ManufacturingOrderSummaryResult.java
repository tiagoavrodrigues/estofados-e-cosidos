package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ManufacturingOrderSummaryResult(
        Long id,
        String code,
        Long customerOrderId,
        Long customerOrderLineId,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity,
        String status,
        LocalDateTime openedAt,
        LocalDateTime completedAt) {
}
