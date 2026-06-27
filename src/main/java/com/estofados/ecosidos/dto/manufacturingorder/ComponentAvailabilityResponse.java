package com.estofados.ecosidos.dto.manufacturingorder;

import java.math.BigDecimal;
import java.util.List;

public record ComponentAvailabilityResponse(
        Long componentPartId,
        String componentPartCode,
        String componentPartName,
        BigDecimal requiredQuantityPerUnit,
        BigDecimal totalRequiredQuantity,
        BigDecimal alreadyAssignedQuantity,
        BigDecimal missingQuantity,
        List<AvailableSemiFinishedOrderResponse> availableSemiFinishedOrders) {
}
