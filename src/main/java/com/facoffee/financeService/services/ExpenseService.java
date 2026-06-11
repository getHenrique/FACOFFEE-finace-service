package com.facoffee.financeService.services;

import com.facoffee.financeService.entities.Expense;
import com.facoffee.financeService.messaging.ExpenseRequest;
import com.facoffee.financeService.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Expense> listAllExpenses() {
        return expenseRepository.findAll(Sort.by(Sort.Direction.DESC, "occurrenceDate"));
    }

    public Expense findExpenseById(String id) {
        return expenseRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Despesa não encontrada com o ID fornecido")
        );
    }

    public void deleteExpense(String id) {
        Expense expense = expenseRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Despesa não encontrada com o ID fornecido")
        );
    }
}
