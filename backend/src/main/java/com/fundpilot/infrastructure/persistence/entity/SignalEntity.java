package com.fundpilot.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "signal", uniqueConstraints = @UniqueConstraint(columnNames = {"fund_id", "signal_date"}))
public class SignalEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private FundEntity fund;

    @Column(nullable = false)
    private LocalDate signalDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalLabel label;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reasons;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String metricsJson;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public FundEntity getFund() { return fund; }
    public void setFund(FundEntity fund) { this.fund = fund; }
    public LocalDate getSignalDate() { return signalDate; }
    public void setSignalDate(LocalDate signalDate) { this.signalDate = signalDate; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public SignalLabel getLabel() { return label; }
    public void setLabel(SignalLabel label) { this.label = label; }
    public String getReasons() { return reasons; }
    public void setReasons(String reasons) { this.reasons = reasons; }
    public String getMetricsJson() { return metricsJson; }
    public void setMetricsJson(String metricsJson) { this.metricsJson = metricsJson; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
