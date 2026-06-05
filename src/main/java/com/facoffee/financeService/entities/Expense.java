package com.facoffee.financeService.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    @Column(name = "attachment_file_path", nullable = false)
    private String attachmentFilePath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Expense() {

        this.createdAt = LocalDateTime.now();

    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAttachmentFilePath() { return attachmentFilePath; }
    public void setAttachmentFilePath(String attachmentFilePath) { this.attachmentFilePath = attachmentFilePath; }

}
