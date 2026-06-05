package com.facoffee.financeService.services;

import com.facoffee.financeService.entities.Pendency;
import com.facoffee.financeService.messaging.PendencyPayloadDTO;
import com.facoffee.financeService.repository.*;
import com.facoffee.financeService.messaging.PendencyEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendencyService {

    private static final Logger log = LoggerFactory.getLogger(PendencyService.class);
    private final PendencyRepository repository;

    public PendencyService(PendencyRepository repository) {
        this.repository = repository;
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

        repository.save(pendency);
        log.info("Pendency created for user ID {} at cycle ({}) with success!", pendency.getUserId(), pendency.getCycle());

    }

}
