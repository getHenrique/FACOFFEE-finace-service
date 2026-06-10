package com.facoffee.financeService.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Converte erros de autorização (@PreAuthorize e checagem de dono) no
 * formato ErrorResponse do contrato, com HTTP 403.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Usuário autenticado não possui permissão para a operação.",
                request.getRequestURI()
        ));
    }
}
