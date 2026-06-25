package com.estofados.ecosidos.dto.customerorder;

import java.math.BigDecimal;

public record CustomerOrderMaterialAvailabilityResponse(
        Long rawMaterialId,
        String rawMaterialCode,
        String rawMaterialName,
        BigDecimal requiredQuantity,
        BigDecimal availableQuantity,
        BigDecimal missingQuantity,
        String unit,
        boolean available
) {
}
