package com.facoffee.financeService.messaging;

import com.facoffee.financeService.services.PendencyService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PendencyEventListener {

    private final PendencyService pendencyService;

    public PendencyEventListener(PendencyService pendencyService) {
        this.pendencyService = pendencyService;
    }

    @RabbitListener(queues = "reporting.finance-pendency-created")
    public void handleNewPendency(PendencyEventDTO event) {
        pendencyService.processNewPendency(event);
    }


}
