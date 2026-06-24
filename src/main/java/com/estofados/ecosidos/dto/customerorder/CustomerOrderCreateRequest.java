package com.estofados.ecosidos.dto.customerorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CustomerOrderCreateRequest(
        @NotNull Long customerId,
        LocalDate orderDate,
        String notes,
        @NotEmpty @Valid List<CustomerOrderLineCreateRequest> lines
) {
}
