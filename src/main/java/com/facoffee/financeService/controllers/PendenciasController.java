package com.facoffee.financeService.controllers;

import com.facoffee.financeService.entities.Pendency;
import com.facoffee.financeService.entities.PendencyStatus;
import com.facoffee.financeService.repository.PendencyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pendencies")
public class PendenciasController {

    private final PendencyRepository pendencyRepository;

    public PendenciasController(PendencyRepository pendencyRepository) {
        this.pendencyRepository = pendencyRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listarPendencias(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String cycle,
            @RequestParam(required = false) PendencyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        List<PendencyResponse> filteredPendencies = pendencyRepository.findAll().stream()
                .filter(pendency -> userId == null || userId.equals(pendency.getUserId()))
                .filter(pendency -> cycle == null || cycle.equals(pendency.getCycle()))
                .filter(pendency -> status == null || status.equals(pendency.getStatus()))
                .sorted(Comparator.comparing(Pendency::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();

        int totalItems = filteredPendencies.size();
        int fromIndex = Math.min(safePage * safeSize, totalItems);
        int toIndex = Math.min(fromIndex + safeSize, totalItems);
        int totalPages = (int) Math.ceil((double) totalItems / safeSize);

        return ResponseEntity.ok(Map.of(
                "items", filteredPendencies.subList(fromIndex, toIndex),
                "page", Map.of(
                        "page", safePage,
                        "size", safeSize,
                        "totalItems", totalItems,
                        "totalPages", totalPages
                )
        ));
    }

    @GetMapping("/{pendencyId}")
    public ResponseEntity<PendencyResponse> consultarPendencyPorId(@PathVariable String pendencyId) {
        return pendencyRepository.findById(pendencyId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private PendencyResponse toResponse(Pendency pendency) {
        return new PendencyResponse(
                pendency.getId(),
                pendency.setChargeSource(),
                pendency.setChargeSourceId(),
                pendency.getUserId(),
                pendency.getCycle(),
                pendency.getAmount(),
                pendency.getStatus(),
                pendency.getCreatedAt()
        );
    }

    public record PendencyResponse(
            String id,
            String source,
            String sourceId,
            String userId,
            String cycle,
            BigDecimal amount,
            PendencyStatus status,
            LocalDateTime createdAt
    ) {
    }
}
