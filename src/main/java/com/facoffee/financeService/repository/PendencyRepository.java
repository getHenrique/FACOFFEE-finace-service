package com.facoffee.financeService.repository;

import com.facoffee.financeService.entities.Pendency;
import com.facoffee.financeService.entities.PendencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PendencyRepository extends JpaRepository<Pendency, String> {

    boolean existsByEventId(String eventId); // Verifica se a mensagem exata do RabbitMQ já foi processada anteriormente

    boolean existsByUserIdAndCycle(String userId, String cycle); // Verifica se o participante já foi cobrado neste ciclo específico

    List<Pendency> findAllByStatusAndDueToBefore(PendencyStatus status, LocalDate currentDate); // Lista todas as pendências de certo status com data de vencimento anterior à fornecida

}
