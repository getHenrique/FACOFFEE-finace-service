package com.facoffee.financeService.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Espelha FinanceStatement do api-docs.yaml. */
public record FinanceStatement(
        Period period,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        List<StatementEntry> entries
) {
    public record Period(LocalDate startDate, LocalDate endDate) {
    }
}
