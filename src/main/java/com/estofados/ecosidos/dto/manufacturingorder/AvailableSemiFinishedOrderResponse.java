package com.estofados.ecosidos.dto.manufacturingorder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AvailableSemiFinishedOrderResponse(
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        BigDecimal quantity,
        Long rackLocationId,
        Long rackId,
        String rackCode,
        String rackName,
        LocalDateTime locatedAt) {
}
