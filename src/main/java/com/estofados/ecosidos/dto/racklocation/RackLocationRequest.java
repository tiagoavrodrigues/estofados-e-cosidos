package com.estofados.ecosidos.dto.racklocation;

import jakarta.validation.constraints.NotNull;

public record RackLocationRequest(
        @NotNull Long manufacturingOrderId,
        @NotNull Long rackId) {
}
