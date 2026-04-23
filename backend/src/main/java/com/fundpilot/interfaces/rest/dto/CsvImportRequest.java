package com.fundpilot.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CsvImportRequest(@NotBlank(message = "csvContent must not be blank") String csvContent) {
}
