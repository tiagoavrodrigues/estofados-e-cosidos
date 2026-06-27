package com.estofados.ecosidos.service.result;

public record ComponentSupplyResult(
        Long componentAssignmentId,
        Long parentManufacturingOrderId,
        String parentManufacturingOrderCode,
        Long componentManufacturingOrderId,
        String componentManufacturingOrderCode,
        Long componentPartId,
        String componentPartCode,
        String componentPartName) {
}
