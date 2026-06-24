package com.estofados.ecosidos.service.input;

import java.math.BigDecimal;

public record CustomerOrderLineCreateInput(
        Long partId,
        BigDecimal quantity
) {
}
