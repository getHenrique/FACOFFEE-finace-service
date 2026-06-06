package com.facoffee.financeService.services;

import com.facoffee.financeService.entities.Pendency;
import com.facoffee.financeService.entities.PendencyStatus;
import com.facoffee.financeService.entities.PendencyStatusChange;
import com.facoffee.financeService.messaging.PendencyPayloadDTO;
import com.facoffee.financeService.repository.*;
import com.facoffee.financeService.messaging.PendencyEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
public class PendencyService {

    private static final Logger log = LoggerFactory.getLogger(PendencyService.class);
    private final PendencyRepository repository;
    private final PendencyStatusChangeRepository statusChangeRepository;

    public PendencyService(PendencyRepository repository, PendencyStatusChangeRepository statusChangeRepository) {

        this.repository = repository;
        this.statusChangeRepository = statusChangeRepository;

    }

    @Transactional
    public void processNewPendency(PendencyEventDTO event) {

        PendencyPayloadDTO payload = event.payload();

        if(repository.existsByEventId(event.eventId())) {
            log.warn("Pendency with event ID {} already processed. Skipping.", event.eventId());
            return;
        }
        if(repository.existsByUserIdAndCycle(payload.userId(), payload.cycle())) {
            log.warn("There's already a pendency for user ID {} at cycle ({}). Skipping.", payload.userId(), payload.cycle());
            return;
        }

        Pendency pendency = new Pendency();
        pendency.setUserId(payload.userId());
        pendency.setCycle(payload.cycle());
        pendency.setAmount(payload.amount());
        pendency.setChargeSource(payload.source());
        pendency.setChargeSourceId(payload.sourceId());
        pendency.setEventId(event.eventId());
        pendency.setDueTo(YearMonth.parse(payload.cycle()).atDay(10)); // O prazo para pagamento é até o dia 10 do mês do ciclo de cobrança

        repository.save(pendency);
        log.info("Pendency created for user ID {} at cycle ({}) with success!", pendency.getUserId(), pendency.getCycle());

    }

    @Transactional
    @Scheduled(cron = "0 1 0,12 * * ?") // Verificar todos os dias às 00:01 da manhã e às 12:01 (meio dia) (horário do servidor)
    //@Scheduled(fixedDelay = 10000) // DEBUG: Verificar a cada 10 segundos. Comentar quando em ambiente de produção.
    public void handleOverdues() {

        log.info("\nStarting scheduled overdue pendency verification...");

        List<Pendency> overduePendencies = repository.findAllByStatusAndDueToBefore(PendencyStatus.PENDING, java.time.LocalDate.now());

        for (Pendency pendency : overduePendencies) {

            PendencyStatusChange statusChange = new PendencyStatusChange();

            statusChange.setPendency(pendency);
            statusChange.setOldStatus(pendency.getStatus());
            pendency.setStatus(PendencyStatus.OVERDUE);
            statusChange.setNewStatus(pendency.getStatus());

            repository.save(pendency);
            statusChangeRepository.save(statusChange);

            log.warn("Pendency ID {} is now OVERDUE.", statusChange.getPendency().getId());

        }

        log.info("Pendencies verified, {} updated.\n", overduePendencies.size());

    }

}
