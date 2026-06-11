package com.facoffee.financeService.services;

import com.facoffee.financeService.entities.*;
import com.facoffee.financeService.messaging.FinanceBalance;
import com.facoffee.financeService.messaging.FinanceStatement;
import com.facoffee.financeService.messaging.StatementEntry;
import com.facoffee.financeService.repository.ExpenseRepository;
import com.facoffee.financeService.repository.PaymentProofRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Relatórios financeiros (api-docs.yaml: /finance/statement e /finance/balance).
 * Entradas (INCOME) = comprovantes VALIDATED. Saídas (EXPENSE) = despesas REGISTERED.
 */
@Service
public class ReportService {

    private final PaymentProofRepository proofRepository;
    private final ExpenseRepository expenseRepository;

    public ReportService(PaymentProofRepository proofRepository, ExpenseRepository expenseRepository) {
        this.proofRepository = proofRepository;
        this.expenseRepository = expenseRepository;
    }

    public FinanceStatement statement(LocalDate startDate, LocalDate endDate) {
        List<StatementEntry> entries = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (PaymentProof p : proofRepository.findAll()) {
            if (p.getStatus() == ProofStatus.VALIDATED && p.getValidatedAt() != null
                    && inRange(p.getValidatedAt().toLocalDate(), startDate, endDate)) {
                totalIncome = totalIncome.add(p.getAmount());
                entries.add(new StatementEntry(p.getId(), StatementEntryType.INCOME,
                        "Comprovante de pagamento validado", p.getAmount(), p.getValidatedAt(),
                        "PAYMENT_PROOF", p.getId()));
            }
        }

        for (Expense e : expenseRepository.findAll()) {
            if (e.getStatus() == ExpenseStatus.REGISTERED && e.getExpenseDate() != null
                    && inRange(e.getExpenseDate(), startDate, endDate)) {
                totalExpense = totalExpense.add(e.getAmount());
                entries.add(new StatementEntry(e.getId(), StatementEntryType.EXPENSE,
                        e.getDescription(), e.getAmount(), e.getExpenseDate().atStartOfDay(),
                        "EXPENSE", e.getId()));
            }
        }

        entries.sort(Comparator.comparing(StatementEntry::occurredAt));
        return new FinanceStatement(
                new FinanceStatement.Period(startDate, endDate),
                totalIncome, totalExpense, totalIncome.subtract(totalExpense), entries);
    }

    public FinanceBalance balance() {
        BigDecimal totalIncome = proofRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProofStatus.VALIDATED)
                .map(PaymentProof::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenseRepository.findAll().stream()
                .filter(e -> e.getStatus() == ExpenseStatus.REGISTERED)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FinanceBalance(totalIncome, totalExpense,
                totalIncome.subtract(totalExpense), LocalDateTime.now());
    }

    private boolean inRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
