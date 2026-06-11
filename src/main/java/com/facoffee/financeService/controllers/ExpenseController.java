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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
