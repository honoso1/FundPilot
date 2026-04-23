package com.fundpilot.interfaces.rest.dto;

import com.fundpilot.infrastructure.persistence.entity.SignalLabel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SignalResponse(LocalDate signalDate, BigDecimal score, SignalLabel label, String reasons, String metricsJson) {
}
