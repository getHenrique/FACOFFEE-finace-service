package com.facoffee.financeService.messaging;

import com.facoffee.financeService.entities.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Espelha CreateExpenseRequest do api-docs.yaml. */
public class ExpenseRequest {

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull(message = "Categoria é obrigatória")
    private ExpenseCategory category;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    @NotNull(message = "Data da despesa é obrigatória")
    private LocalDate expenseDate;

    @NotBlank(message = "registeredBy é obrigatório")
    private String registeredBy;

    private String receiptUrl;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(String registeredBy) { this.registeredBy = registeredBy; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
}
