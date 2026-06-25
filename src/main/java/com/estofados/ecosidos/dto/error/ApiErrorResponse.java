package com.estofados.ecosidos.dto.error;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}
