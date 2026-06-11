package com.facoffee.financeService.services;

import com.facoffee.financeService.entities.Expense;
import com.facoffee.financeService.entities.ExpenseCategory;
import com.facoffee.financeService.messaging.ExpenseRequest;
import com.facoffee.financeService.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense createExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCycle(YearMonth.from(request.getExpenseDate()).toString());
        expense.setRegisteredBy(request.getRegisteredBy());
        expense.setReceiptUrl(request.getReceiptUrl());
        return expenseRepository.save(expense);
    }

    /** Lista despesas com filtros opcionais (cycle, category), ordenadas por data desc (US-7). */
    public List<Expense> listExpenses(String cycle, ExpenseCategory category) {
        return expenseRepository.findAll().stream()
                .filter(e -> cycle == null || cycle.equals(e.getCycle()))
                .filter(e -> category == null || category == e.getCategory())
                .sorted(Comparator.comparing(Expense::getExpenseDate).reversed())
                .toList();
    }

    public Optional<Expense> findById(String id) {
        return expenseRepository.findById(id);
    }

    /** Remove a despesa; retorna false se não existir (para o controller responder 404). */
    public boolean deleteExpense(String id) {
        if (!expenseRepository.existsById(id)) {
            return false;
        }
        expenseRepository.deleteById(id);
        return true;
    }
}
