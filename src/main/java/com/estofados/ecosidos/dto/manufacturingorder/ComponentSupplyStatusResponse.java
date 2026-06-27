package com.estofados.ecosidos.dto.manufacturingorder;

import java.math.BigDecimal;
import java.util.List;

public record ComponentSupplyStatusResponse(
        Long componentPartId,
        String componentPartCode,
        String componentPartName,
        BigDecimal requiredQuantityPerUnit,
        BigDecimal totalRequiredQuantity,
        BigDecimal suppliedQuantity,
        BigDecimal missingQuantity,
        boolean fullySupplied,
        List<SuppliedSemiFinishedOrderResponse> suppliedOrders) {
}
