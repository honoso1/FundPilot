package com.fundpilot.infrastructure.persistence.repository;

import com.fundpilot.infrastructure.persistence.entity.FundEntity;
import com.fundpilot.infrastructure.persistence.entity.SignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SignalRepository extends JpaRepository<SignalEntity, UUID> {
    Optional<SignalEntity> findTopByFundOrderBySignalDateDesc(FundEntity fund);
    Optional<SignalEntity> findByFundAndSignalDate(FundEntity fund, java.time.LocalDate signalDate);
}
