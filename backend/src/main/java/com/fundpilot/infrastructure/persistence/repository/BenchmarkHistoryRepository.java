package com.fundpilot.infrastructure.persistence.repository;

import com.fundpilot.infrastructure.persistence.entity.BenchmarkHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BenchmarkHistoryRepository extends JpaRepository<BenchmarkHistoryEntity, UUID> {
    Optional<BenchmarkHistoryEntity> findByBenchmarkCodeAndBenchmarkDate(String benchmarkCode, LocalDate benchmarkDate);
    List<BenchmarkHistoryEntity> findByBenchmarkCodeOrderByBenchmarkDateAsc(String benchmarkCode);
    List<BenchmarkHistoryEntity> findByBenchmarkCodeAndBenchmarkDateLessThanEqualOrderByBenchmarkDateAsc(String benchmarkCode, LocalDate benchmarkDate);
}
