package com.facoffee.financeService.controllers;

import com.facoffee.financeService.messaging.FinanceBalance;
import com.facoffee.financeService.messaging.FinanceStatement;
import com.facoffee.financeService.services.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Relatórios financeiros (api-docs.yaml: /finance/statement e /finance/balance).
 * Context-path /api/finance, então os mapeamentos são "/statement" e "/balance".
 */
@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Consultar extrato de entradas e saídas do período (somente MANAGER)
    @GetMapping("/statement")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Object> getStatement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest http) {
        if (endDate.isBefore(startDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                    Instant.now().toString(), HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "endDate não pode ser anterior a startDate.", http.getRequestURI()));
        }
        FinanceStatement statement = reportService.statement(startDate, endDate);
        return ResponseEntity.ok(statement);
    }

    // Consultar balanço consolidado histórico (somente MANAGER)
    @GetMapping("/balance")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<FinanceBalance> getBalance() {
        return ResponseEntity.ok(reportService.balance());
    }
}
