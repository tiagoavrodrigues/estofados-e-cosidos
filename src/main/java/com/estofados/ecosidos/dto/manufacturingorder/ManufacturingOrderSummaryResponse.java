package com.estofados.ecosidos.dto.manufacturingorder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ManufacturingOrderSummaryResponse(
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
