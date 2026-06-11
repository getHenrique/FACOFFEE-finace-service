package com.facoffee.financeService.messaging;

import com.facoffee.financeService.entities.StatementEntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Espelha StatementEntry do api-docs.yaml. */
public record StatementEntry(
        String id,
        StatementEntryType type,
        String description,
        BigDecimal amount,
        LocalDateTime occurredAt,
        String referenceType,
        String referenceId
) {
}
