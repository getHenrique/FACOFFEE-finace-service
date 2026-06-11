package com.facoffee.financeService.controllers;

import com.facoffee.financeService.entities.*;
import com.facoffee.financeService.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * US-4 (#12) e US-5 (#13) - Comprovantes de pagamento.
 * Contrato: facoffee-docs/api-docs.yaml (paths /finance/pendencies/{id}/proofs).
 */
@RestController
@RequestMapping("/pendencies")
public class PaymentProofController {

    private final PaymentProofRepository proofRepository;
    private final PendencyRepository pendencyRepository;
    private final ProofStatusChangeRepository proofStatusChangeRepository;

    public PaymentProofController(PaymentProofRepository proofRepository,
                                  PendencyRepository pendencyRepository,
                                  ProofStatusChangeRepository proofStatusChangeRepository) {
        this.proofRepository = proofRepository;
        this.pendencyRepository = pendencyRepository;
        this.proofStatusChangeRepository = proofStatusChangeRepository;
    }

    // US-4: Participante envia comprovante
    @PostMapping("/{pendencyId}/proofs")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<Object> submitProof(@PathVariable String pendencyId,
                                              @RequestBody SubmitPaymentProofRequest request,
                                              HttpServletRequest http) {
        Pendency pendency = pendencyRepository.findById(pendencyId).orElse(null);
        if (pendency == null) return notFound(http);

        if (request.submittedBy() == null || request.amount() == null || request.paymentDate() == null
                || request.method() == null || request.receiptUrl() == null) {
            return error(HttpStatus.BAD_REQUEST, "Campos obrigatórios ausentes.", http);
        }

        // Permitido enquanto a pendência não estiver paga e não houver comprovante aguardando validação.
        boolean blocked = pendency.getStatus() == PendencyStatus.PAID
                || proofRepository.findByPendencyId(pendencyId).stream()
                        .anyMatch(p -> p.getStatus() == ProofStatus.WAITING_VALIDATION);
        if (blocked) {
            return error(HttpStatus.CONFLICT, "A pendência não permite novo comprovante no estado atual.", http);
        }

        PaymentProof proof = new PaymentProof();
        proof.setPendency(pendency);
        proof.setUserId(request.submittedBy());
        proof.setAmount(request.amount());
        proof.setPaymentDate(request.paymentDate());
        proof.setMethod(request.method());
        proof.setReceiptUrl(request.receiptUrl());
        proof.setNote(request.note());

        proofRepository.save(proof);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(proof));
    }

    // US-5: Listar comprovantes (qualquer usuário autenticado)
    @GetMapping("/{pendencyId}/proofs")
    public ResponseEntity<Object> listProofs(@PathVariable String pendencyId,
                                             @RequestParam(required = false) ProofStatus status,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             HttpServletRequest http) {
        if (!pendencyRepository.existsById(pendencyId)) return notFound(http);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        List<PaymentProofResponse> proofs = proofRepository.findByPendencyId(pendencyId).stream()
                .filter(p -> status == null || status == p.getStatus())
                .sorted(Comparator.comparing(PaymentProof::getSubmittedAt).reversed())
                .map(this::toResponse)
                .toList();

        int total = proofs.size();
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);
        int totalPages = (int) Math.ceil((double) total / safeSize);

        return ResponseEntity.ok(Map.of(
                "items", proofs.subList(from, to),
                "page", Map.of(
                        "page", safePage,
                        "size", safeSize,
                        "totalElements", total,
                        "totalPages", totalPages
                )
        ));
    }

    // US-5: Gerente decide o comprovante
    @PatchMapping("/{pendencyId}/proofs/{proofId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Object> decideProof(@PathVariable String pendencyId,
                                              @PathVariable String proofId,
                                              @RequestBody PaymentProofDecisionRequest request,
                                              HttpServletRequest http) {
        Pendency pendency = pendencyRepository.findById(pendencyId).orElse(null);
        if (pendency == null) return notFound(http);

        PaymentProof proof = proofRepository.findById(proofId).orElse(null);
        if (proof == null) return notFound(http);

        if (proof.getStatus() != ProofStatus.WAITING_VALIDATION) {
            return error(HttpStatus.CONFLICT, "O comprovante não pode ser decidido no estado atual.", http);
        }
        if (request.decidedBy() == null || request.decidedBy().isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "Campo 'decidedBy' é obrigatório.", http);
        }

        ProofStatus oldStatus = proof.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if (request.status() == ProofStatus.VALIDATED) {
            proof.setStatus(ProofStatus.VALIDATED);
            proof.setValidatedBy(request.decidedBy());
            proof.setValidatedAt(now);
            pendency.setStatus(PendencyStatus.PAID);
        } else if (request.status() == ProofStatus.REJECTED) {
            if (request.reason() == null || request.reason().isBlank()) {
                return error(HttpStatus.BAD_REQUEST, "Campo 'reason' é obrigatório para rejeição.", http);
            }
            proof.setStatus(ProofStatus.REJECTED);
            proof.setRejectedBy(request.decidedBy());
            proof.setRejectedAt(now);
            proof.setRejectionReason(request.reason());
            pendency.setStatus(PendencyStatus.PENDING); // volta para estado de cobrança
        } else {
            return error(HttpStatus.BAD_REQUEST, "Status inválido. Use VALIDATED ou REJECTED.", http);
        }

        ProofStatusChange change = new ProofStatusChange();
        change.setPaymentProof(proof);
        change.setOldStatus(oldStatus);
        change.setNewStatus(proof.getStatus());
        change.setManagerId(request.decidedBy());
        change.setDescription(request.note() != null ? request.note()
                : (request.reason() != null ? request.reason() : proof.getStatus().name()));

        proofStatusChangeRepository.save(change);
        pendencyRepository.save(pendency);
        proofRepository.save(proof);

        return ResponseEntity.ok(toResponse(proof));
    }

    // US-5: Remover comprovante (gerente OU dono do comprovante)
    @DeleteMapping("/{pendencyId}/proofs/{proofId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('PARTICIPANT')")
    public ResponseEntity<Object> deleteProof(@PathVariable String pendencyId,
                                              @PathVariable String proofId,
                                              @AuthenticationPrincipal Jwt jwt,
                                              HttpServletRequest http) {
        if (!pendencyRepository.existsById(pendencyId)) return notFound(http);

        PaymentProof proof = proofRepository.findById(proofId).orElse(null);
        if (proof == null) return notFound(http);

        if (!isManager(jwt) && !isOwner(jwt, proof)) {
            throw new AccessDeniedException("Apenas o gestor ou o dono do comprovante pode removê-lo.");
        }

        if (proof.getStatus() != ProofStatus.WAITING_VALIDATION) {
            return error(HttpStatus.CONFLICT, "Não é possível remover comprovante que já foi analisado.", http);
        }

        proofRepository.delete(proof);
        return ResponseEntity.noContent().build();
    }

    // ----- helpers -----

    private boolean isManager(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        return realmAccess != null
                && realmAccess.get("roles") instanceof List<?> roles
                && roles.contains("MANAGER");
    }

    private boolean isOwner(Jwt jwt, PaymentProof proof) {
        if (proof.getUserId() == null) return false;
        return proof.getUserId().equals(jwt.getSubject())
                || proof.getUserId().equals(jwt.getClaimAsString("preferred_username"));
    }

    private ResponseEntity<Object> notFound(HttpServletRequest http) {
        return error(HttpStatus.NOT_FOUND, "Recurso não encontrado.", http);
    }

    private ResponseEntity<Object> error(HttpStatus status, String message, HttpServletRequest http) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now().toString(), status.value(), status.getReasonPhrase(), message, http.getRequestURI()));
    }

    private PaymentProofResponse toResponse(PaymentProof p) {
        return new PaymentProofResponse(
                p.getId(),
                p.getPendency().getId(),
                p.getUserId(),
                p.getAmount(),
                p.getPaymentDate(),
                p.getMethod(),
                p.getReceiptUrl(),
                p.getStatus(),
                p.getNote(),
                p.getSubmittedAt(),
                p.getValidatedBy(),
                p.getValidatedAt(),
                p.getRejectedBy(),
                p.getRejectedAt(),
                p.getRejectionReason()
        );
    }

    // ----- DTOs (espelham o contrato api-docs.yaml) -----

    public record SubmitPaymentProofRequest(
            String submittedBy,
            BigDecimal amount,
            LocalDate paymentDate,
            PaymentMethod method,
            String receiptUrl,
            String note
    ) {}

    public record PaymentProofDecisionRequest(
            ProofStatus status,
            String decidedBy,
            String note,
            String reason
    ) {}

    public record PaymentProofResponse(
            String id,
            String pendencyId,
            String userId,
            BigDecimal amount,
            LocalDate paymentDate,
            PaymentMethod method,
            String receiptUrl,
            ProofStatus status,
            String note,
            LocalDateTime submittedAt,
            String validatedBy,
            LocalDateTime validatedAt,
            String rejectedBy,
            LocalDateTime rejectedAt,
            String rejectionReason
    ) {}
}
