package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;

public record CustomerOrderMaterialAvailabilityResult(
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
