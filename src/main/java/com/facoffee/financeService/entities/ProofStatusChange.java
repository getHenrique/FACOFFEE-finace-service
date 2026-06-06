package com.facoffee.financeService.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "proof_status_change")
public class ProofStatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private ProofStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private ProofStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne
    @JoinColumn(name = "payment_proof_id", nullable = false)
    private PaymentProof paymentProof;

    @Column(name = "manager_id", nullable = false)
    private String managerId;

    @Column(nullable = false)
    private String description;

    public ProofStatusChange() {
        this.changedAt = LocalDateTime.now();
    }

    //Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ProofStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(ProofStatus oldStatus) { this.oldStatus = oldStatus; }
    public ProofStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ProofStatus newStatus) { this.newStatus = newStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    public PaymentProof getPaymentProof() { return paymentProof; }
    public void setPaymentProof(PaymentProof paymentProof) { this.paymentProof = paymentProof; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}
