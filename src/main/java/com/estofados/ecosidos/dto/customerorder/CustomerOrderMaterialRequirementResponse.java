package com.estofados.ecosidos.dto.customerorder;

import java.math.BigDecimal;

public record CustomerOrderMaterialRequirementResponse(
        Long rawMaterialId,
        String rawMaterialCode,
        String rawMaterialName,
        BigDecimal requiredQuantity,
        String unit
) {
}
