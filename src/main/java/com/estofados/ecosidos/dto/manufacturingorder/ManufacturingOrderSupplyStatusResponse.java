package com.estofados.ecosidos.dto.manufacturingorder;

import java.math.BigDecimal;
import java.util.List;

public record ManufacturingOrderSupplyStatusResponse(
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity,
        boolean fullySupplied,
        List<ComponentSupplyStatusResponse> components) {
}
