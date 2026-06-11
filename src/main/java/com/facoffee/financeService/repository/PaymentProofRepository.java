package com.facoffee.financeService.repository;

import com.facoffee.financeService.entities.PaymentProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentProofRepository extends JpaRepository<PaymentProof, String> {

    List<PaymentProof> findByPendencyId(String pendencyId);

}
