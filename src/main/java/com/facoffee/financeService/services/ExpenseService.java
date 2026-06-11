package com.facoffee.financeService.services;

import com.facoffee.financeService.entities.Expense;
import com.facoffee.financeService.messaging.ExpenseRequest;
import com.facoffee.financeService.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public Expense createExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setOccurrenceDate(request.getOccurrenceDate());
        expense.setAmount(request.getAmount());
        expense.setAttachmentFilePath(request.getAttachmentFilePath());

        return expenseRepository.save(expense);
    }
}
