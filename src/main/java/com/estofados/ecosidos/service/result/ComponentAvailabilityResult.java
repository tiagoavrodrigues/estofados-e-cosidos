package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;
import java.util.List;

public record ComponentAvailabilityResult(
        Long componentPartId,
        String componentPartCode,
        String componentPartName,
        BigDecimal requiredQuantityPerUnit,
        BigDecimal totalRequiredQuantity,
        BigDecimal alreadyAssignedQuantity,
        BigDecimal missingQuantity,
        List<AvailableSemiFinishedOrderResult> availableSemiFinishedOrders) {
}
