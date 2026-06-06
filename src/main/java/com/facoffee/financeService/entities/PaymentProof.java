package com.facoffee.financeService.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_proof")
public class PaymentProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @ManyToOne
    @JoinColumn(name = "pendency_id", nullable = false)
    private Pendency pendency;

    @Column(name = "attachment_file_path", nullable = false)
    private String attachmentFilePath;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProofStatus status;

    public PaymentProof() {
        this.status = ProofStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Pendency getPendency() { return pendency; }
    public void setPendency(Pendency pendency) { this.pendency = pendency; }
    public String getAttachmentFilePath() { return attachmentFilePath; }
    public void setAttachmentFilePath(String attachmentFilePath) { this.attachmentFilePath = attachmentFilePath; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public ProofStatus getStatus() { return status; }
    public void setStatus(ProofStatus status) { this.status = status; }

}
