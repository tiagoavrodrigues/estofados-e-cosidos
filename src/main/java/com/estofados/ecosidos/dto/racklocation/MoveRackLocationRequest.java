package com.estofados.ecosidos.dto.racklocation;

import jakarta.validation.constraints.NotNull;

public record MoveRackLocationRequest(
        @NotNull Long rackId) {
}
