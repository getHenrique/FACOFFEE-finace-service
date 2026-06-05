package com.facoffee.financeService.messaging;

import java.math.BigDecimal;

public record PendencyPayloadDTO(
        String pendencyId,
        String source,
        String sourceId,
        String userId,
        String cycle,
        BigDecimal amount,
        String status
) { }
