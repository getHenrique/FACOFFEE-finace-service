package com.facoffee.financeService.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pendency")
public class Pendency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String cycle; //format: YYYY-MM

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PendencyStatus status;

    // Campos de Auditoria exigidos no escopo
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "origin_event")
    private String originEvent;

    @Column(name = "id_event")
    private String idEvent;

    @OneToMany(mappedBy = "pendency", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentProof> proofs = new ArrayList<>();

    public Pendency() {
        this.status = PendencyStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    //Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PendencyStatus getStatus() { return status; }
    public void setStatus(PendencyStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getOriginEvent() { return originEvent; }
    public void setOriginEvent(String originEvent) { this.originEvent = originEvent; }
    public String getIdEvent() { return idEvent; }
    public void setIdEvent(String idEvent) { this.idEvent = idEvent; }
    public List<PaymentProof> getProofs() { return proofs; }


}
