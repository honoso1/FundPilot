package com.fundpilot.infrastructure.persistence.repository;

import com.fundpilot.infrastructure.persistence.entity.FundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FundRepository extends JpaRepository<FundEntity, UUID> {
    Optional<FundEntity> findByCode(String code);
}
