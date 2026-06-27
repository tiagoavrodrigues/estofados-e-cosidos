package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;
import java.util.List;

public record ManufacturingOrderSupplyStatusResult(
        Long manufacturingOrderId,
        String manufacturingOrderCode,
        Long partId,
        String partCode,
        String partName,
        BigDecimal quantity,
        boolean fullySupplied,
        List<ComponentSupplyStatusResult> components) {
}
