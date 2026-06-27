package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;
import java.util.List;

public record ManufacturingOrderComponentAvailabilityResult(
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity,
        List<ComponentAvailabilityResult> components) {
}
