package com.facoffee.financeService.controllers;

/**
 * Formato de erro padrão do contrato (api-docs.yaml -> ErrorResponse).
 */
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
