package com.facoffee.financeService.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pendency_status_change")
public class PendencyStatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private PendencyStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private PendencyStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne
    @JoinColumn(name = "pendency_id", nullable = false)
    private Pendency pendency;

    public PendencyStatusChange() {
        this.changedAt = LocalDateTime.now();
    }

    //Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public PendencyStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(PendencyStatus oldStatus) { this.oldStatus = oldStatus; }
    public PendencyStatus getNewStatus() { return newStatus; }
    public void setNewStatus(PendencyStatus newStatus) { this.newStatus = newStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    public Pendency getPendency() { return pendency; }
    public void setPendency(Pendency pendency) { this.pendency = pendency; }

}
