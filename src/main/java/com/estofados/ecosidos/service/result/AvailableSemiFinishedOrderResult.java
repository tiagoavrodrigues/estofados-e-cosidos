package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AvailableSemiFinishedOrderResult(
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        BigDecimal quantity,
        Long rackLocationId,
        Long rackId,
        String rackCode,
        String rackName,
        LocalDateTime locatedAt) {
}
