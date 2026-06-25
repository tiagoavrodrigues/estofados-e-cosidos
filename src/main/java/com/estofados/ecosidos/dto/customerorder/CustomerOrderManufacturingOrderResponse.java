package com.estofados.ecosidos.dto.customerorder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerOrderManufacturingOrderResponse(
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
