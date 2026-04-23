package com.fundpilot.application.dto;

import com.fundpilot.infrastructure.persistence.entity.SignalLabel;

import java.math.BigDecimal;
import java.util.List;

public record ScoreResult(BigDecimal score, SignalLabel label, List<String> reasons, String metricsJson) {
}
