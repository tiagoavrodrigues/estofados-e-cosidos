package com.estofados.ecosidos.dto.manufacturingorder;

import java.math.BigDecimal;
import java.util.List;

public record ManufacturingOrderComponentAvailabilityResponse(
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity,
        List<ComponentAvailabilityResponse> components) {
}
