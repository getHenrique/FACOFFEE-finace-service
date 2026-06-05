package com.facoffee.financeService.repository;

import com.facoffee.financeService.entities.Pendency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendencyRepository extends JpaRepository<Pendency, String> {

    boolean existsByIdEvent(String eventId); // Verifica se a mensagem exata do RabbitMQ já foi processada anteriormente

    boolean existsByUserIdAndCycle(String userId, String cycle); // Verifica se o participante já foi cobrado neste ciclo específico

}
