package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RackLocationResult(
        Long rackLocationId,
        Long rackId,
        String rackCode,
        String rackName,
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity,
        LocalDateTime locatedAt) {
}
