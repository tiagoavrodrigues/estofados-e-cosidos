package com.estofados.ecosidos.dto.manufacturingorder;

import jakarta.validation.constraints.NotNull;

public record SupplyComponentsRequest(
        @NotNull Long semiFinishedManufacturingOrderId) {
}
