package com.estofados.ecosidos.domain;

public enum CustomerOrderStatusCode {
    RECEIVED,
    VALIDATED,
    WAITING_FOR_MATERIAL,
    READY_FOR_PRODUCTION,
    IN_PREPARATION,
    IN_PRODUCTION,
    IN_QUALITY_CONTROL,
    PACKAGED,
    SHIPPED,
    CANCELLED
}
