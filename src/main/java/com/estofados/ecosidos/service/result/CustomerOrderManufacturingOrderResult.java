package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerOrderManufacturingOrderResult(
        Long id,
        String code,
        Long customerOrderLineId,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity,
        String status,
        LocalDateTime openedAt,
        LocalDateTime completedAt) {
}
