package com.facoffee.financeService.controllers;

import com.facoffee.financeService.entities.Expense;
import com.facoffee.financeService.entities.ExpenseCategory;
import com.facoffee.financeService.messaging.ExpenseRequest;
import com.facoffee.financeService.messaging.ExpenseResponse;
import com.facoffee.financeService.services.ExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * US-6 (#14) e US-7 (#15) - Despesas da copa.
 * Contrato: facoffee-docs/api-docs.yaml (paths /finance/expenses).
 * O context-path já é /api/finance, então o mapeamento é apenas "/expenses".
 */
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // US-6: cadastrar despesa (somente MANAGER)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Object> createExpense(@Valid @RequestBody ExpenseRequest request) {
        Expense expense = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExpenseResponse.from(expense));
    }

    // US-7: listar despesas (qualquer autenticado), filtros opcionais + paginação
    @GetMapping
    public ResponseEntity<Object> listExpenses(@RequestParam(required = false) String cycle,
                                               @RequestParam(required = false) ExpenseCategory category,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        List<ExpenseResponse> all = expenseService.listExpenses(cycle, category).stream()
                .map(ExpenseResponse::from)
                .toList();

        int total = all.size();
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);
        int totalPages = (int) Math.ceil((double) total / safeSize);

        return ResponseEntity.ok(Map.of(
                "items", all.subList(from, to),
                "page", Map.of(
                        "page", safePage,
                        "size", safeSize,
                        "totalElements", total,
                        "totalPages", totalPages
                )
        ));
    }

    // US-7: obter despesa por id (qualquer autenticado)
    @GetMapping("/{expenseId}")
    public ResponseEntity<Object> getExpenseById(@PathVariable String expenseId, HttpServletRequest http) {
        return expenseService.findById(expenseId)
                .<ResponseEntity<Object>>map(e -> ResponseEntity.ok(ExpenseResponse.from(e)))
                .orElseGet(() -> notFound(http));
    }

    // US-7: remover despesa (somente MANAGER)
    @DeleteMapping("/{expenseId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Object> deleteExpense(@PathVariable String expenseId, HttpServletRequest http) {
        if (!expenseService.deleteExpense(expenseId)) {
            return notFound(http);
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Object> notFound(HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                Instant.now().toString(), HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(), "Recurso não encontrado.", http.getRequestURI()));
    }
}
