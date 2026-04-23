package com.fundpilot.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NavHistoryResponse(LocalDate date, BigDecimal nav) {
}
