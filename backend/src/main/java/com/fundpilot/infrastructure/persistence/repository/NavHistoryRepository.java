package com.fundpilot.infrastructure.persistence.repository;

import com.fundpilot.infrastructure.persistence.entity.FundEntity;
import com.fundpilot.infrastructure.persistence.entity.NavHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NavHistoryRepository extends JpaRepository<NavHistoryEntity, UUID> {
    Optional<NavHistoryEntity> findByFundAndNavDate(FundEntity fund, LocalDate navDate);
    List<NavHistoryEntity> findByFundOrderByNavDateAsc(FundEntity fund);
    List<NavHistoryEntity> findByFundOrderByNavDateDesc(FundEntity fund);
}
