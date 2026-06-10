package com.facoffee.financeService.messaging;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {
    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull(message = "Data da ocorrência é obrigatória")
    private LocalDate occurrenceDate;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    private String attachmentFilePath;
}
