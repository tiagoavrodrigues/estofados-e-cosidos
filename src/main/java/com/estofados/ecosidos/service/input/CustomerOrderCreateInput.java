package com.estofados.ecosidos.service.input;

import java.time.LocalDate;
import java.util.List;

public record CustomerOrderCreateInput(
        Long customerId,
        LocalDate orderDate,
        String notes,
        List<CustomerOrderLineCreateInput> lines
) {
}
