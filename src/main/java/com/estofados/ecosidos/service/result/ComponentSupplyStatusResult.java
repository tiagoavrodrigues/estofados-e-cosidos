package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;
import java.util.List;

public record ComponentSupplyStatusResult(
        Long componentPartId,
        String componentPartCode,
        String componentPartName,
        BigDecimal requiredQuantityPerUnit,
        BigDecimal totalRequiredQuantity,
        BigDecimal suppliedQuantity,
        BigDecimal missingQuantity,
        boolean fullySupplied,
        List<SuppliedSemiFinishedOrderResult> suppliedOrders) {
}
