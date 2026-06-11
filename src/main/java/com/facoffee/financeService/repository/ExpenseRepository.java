package com.facoffee.financeService.repository;

import com.facoffee.financeService.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, String>{
}
