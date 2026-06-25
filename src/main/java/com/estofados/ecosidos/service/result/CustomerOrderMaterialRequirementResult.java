package com.estofados.ecosidos.service.result;

import java.math.BigDecimal;

public record CustomerOrderMaterialRequirementResult(
        Long rawMaterialId,
        String rawMaterialCode,
        String rawMaterialName,
        BigDecimal requiredQuantity,
        String unit
) {
}
