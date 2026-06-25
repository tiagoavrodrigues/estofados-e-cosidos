package com.estofados.ecosidos.dto.customerorder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CustomerOrderLineCreateRequest(
        @NotNull Long partId,
        @NotNull @Positive BigDecimal quantity
) {
}
