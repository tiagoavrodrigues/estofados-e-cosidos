package com.estofados.ecosidos.dto.manufacturingorder;

public record ComponentSupplyResponse(
        Long componentAssignmentId,
        Long parentManufacturingOrderId,
        String parentManufacturingOrderCode,
        Long componentManufacturingOrderId,
        String componentManufacturingOrderCode,
        Long componentPartId,
        String componentPartCode,
        String componentPartName) {
}
