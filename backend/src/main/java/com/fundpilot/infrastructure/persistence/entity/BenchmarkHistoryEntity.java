package com.fundpilot.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "benchmark_history", uniqueConstraints = @UniqueConstraint(columnNames = {"benchmark_code", "benchmark_date"}))
public class BenchmarkHistoryEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String benchmarkCode;

    @Column(nullable = false)
    private LocalDate benchmarkDate;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal benchmarkValue;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getBenchmarkCode() { return benchmarkCode; }
    public void setBenchmarkCode(String benchmarkCode) { this.benchmarkCode = benchmarkCode; }
    public LocalDate getBenchmarkDate() { return benchmarkDate; }
    public void setBenchmarkDate(LocalDate benchmarkDate) { this.benchmarkDate = benchmarkDate; }
    public BigDecimal getBenchmarkValue() { return benchmarkValue; }
    public void setBenchmarkValue(BigDecimal benchmarkValue) { this.benchmarkValue = benchmarkValue; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
