package com.fundpilot.common.api;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(String code,
                            String message,
                            Map<String, String> validation,
                            OffsetDateTime timestamp,
                            String path) {
}
