package com.fundpilot.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "nav_history", uniqueConstraints = @UniqueConstraint(columnNames = {"fund_id", "nav_date"}))
public class NavHistoryEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private FundEntity fund;

    @Column(nullable = false)
    private LocalDate navDate;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal navValue;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public FundEntity getFund() { return fund; }
    public void setFund(FundEntity fund) { this.fund = fund; }
    public LocalDate getNavDate() { return navDate; }
    public void setNavDate(LocalDate navDate) { this.navDate = navDate; }
    public BigDecimal getNavValue() { return navValue; }
    public void setNavValue(BigDecimal navValue) { this.navValue = navValue; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
