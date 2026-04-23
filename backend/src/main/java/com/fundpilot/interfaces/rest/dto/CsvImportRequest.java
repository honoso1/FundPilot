package com.fundpilot.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CsvImportRequest(@NotBlank String csvContent) {
}
