package com.fundpilot.interfaces.rest.dto;

import com.fundpilot.infrastructure.persistence.entity.SignalLabel;

import java.math.BigDecimal;
import java.util.UUID;

public record FundSummaryResponse(UUID id, String code, String name, BigDecimal latestNav, BigDecimal latestScore, SignalLabel latestLabel) {
}
