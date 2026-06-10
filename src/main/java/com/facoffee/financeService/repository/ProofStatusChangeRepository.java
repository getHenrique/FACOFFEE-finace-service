package com.facoffee.financeService.repository;

import com.facoffee.financeService.entities.ProofStatusChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProofStatusChangeRepository extends JpaRepository<ProofStatusChange, String> {
}
