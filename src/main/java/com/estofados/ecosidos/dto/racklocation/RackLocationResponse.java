package com.estofados.ecosidos.dto.racklocation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RackLocationResponse(
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
