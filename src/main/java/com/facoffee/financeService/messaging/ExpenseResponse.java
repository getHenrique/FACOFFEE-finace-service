package com.facoffee.financeService.messaging;

import com.facoffee.financeService.entities.Expense;
import com.facoffee.financeService.entities.ExpenseCategory;
import com.facoffee.financeService.entities.ExpenseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Espelha o schema Expense do api-docs.yaml. */
public record ExpenseResponse(
        String id,
        String description,
        ExpenseCategory category,
        BigDecimal amount,
        LocalDate expenseDate,
        String cycle,
        ExpenseStatus status,
        String registeredBy,
        String receiptUrl,
        LocalDateTime createdAt
) {
    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getDescription(),
                e.getCategory(),
                e.getAmount(),
                e.getExpenseDate(),
                e.getCycle(),
                e.getStatus(),
                e.getRegisteredBy(),
                e.getReceiptUrl(),
                e.getCreatedAt()
        );
    }
}
