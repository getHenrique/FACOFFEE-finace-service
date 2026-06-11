package com.facoffee.financeService.controllers;

import com.facoffee.financeService.entities.Expense;
import com.facoffee.financeService.messaging.ExpenseRequest;
import com.facoffee.financeService.messaging.ExpenseResponse;
import com.facoffee.financeService.services.ExpenseService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping("/expenses")
    @RolesAllowed("MANAGER")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        Expense expense = expenseService.createExpense(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ExpenseResponse(expense));
    }

    @GetMapping("/expenses")
    @RolesAllowed("MANAGER")
    public ResponseEntity<List<ExpenseResponse>> listExpenses() {
        List<Expense> expenses = expenseService.listAllExpenses();
        List<ExpenseResponse> response = expenses.stream().map(ExpenseResponse::new).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/expenses/{expenseId}")
    @RolesAllowed("MANAGER")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable String expenseId) {
        Expense expense = expenseService.findExpenseById(expenseId);

        return ResponseEntity.ok(new ExpenseResponse(expense));
    }
}
