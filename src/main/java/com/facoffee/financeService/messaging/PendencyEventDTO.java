package com.facoffee.financeService.messaging;

public record PendencyEventDTO(
        String eventId,
        String eventType,
        String occurredAt,
        String version,
        PendencyPayloadDTO payload
) { }
