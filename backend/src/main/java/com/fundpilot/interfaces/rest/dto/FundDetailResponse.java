package com.fundpilot.interfaces.rest.dto;

import java.util.UUID;

public record FundDetailResponse(UUID id, String code, String name, String benchmarkCode) {
}
