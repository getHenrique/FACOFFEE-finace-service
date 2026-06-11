package com.facoffee.financeService.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Espelha FinanceBalance do api-docs.yaml. */
public record FinanceBalance(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        LocalDateTime updatedAt
) {
}
