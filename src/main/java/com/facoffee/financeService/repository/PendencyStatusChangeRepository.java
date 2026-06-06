package com.facoffee.financeService.repository;

import com.facoffee.financeService.entities.PendencyStatusChange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendencyStatusChangeRepository extends JpaRepository<PendencyStatusChange, Integer> {

    // Default methods from the interface this derives

}
