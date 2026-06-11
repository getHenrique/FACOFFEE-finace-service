package com.facoffee.financeService.messaging;

import com.facoffee.financeService.entities.Expense;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ExpenseResponse {
    private String id;
    private String description;
    private BigDecimal amount;
    private LocalDate occurrenceDate;
    private String attachmentFilePath;
    private LocalDateTime createdAt;

    public ExpenseResponse(Expense expense) {
        this.id = expense.getId();
        this.description = expense.getDescription();
        this.amount = expense.getAmount();
        this.occurrenceDate = expense.getOccurrenceDate();
        this.attachmentFilePath = expense.getAttachmentFilePath();
        this.createdAt = expense.getCreatedAt();
    }
}
