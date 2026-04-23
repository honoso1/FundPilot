package com.fundpilot.interfaces.rest;

import com.fundpilot.application.service.IngestionService;
import com.fundpilot.interfaces.rest.dto.CsvImportRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/import")
public class ImportController {
    private final IngestionService ingestionService;

    public ImportController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/mock")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> importMock() {
        ingestionService.importMock();
        return Map.of("status", "mock import completed");
    }

    @PostMapping("/csv")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> importCsv(@Valid @RequestBody CsvImportRequest request) {
        ingestionService.importCsv(request.csvContent());
        return Map.of("status", "csv import completed");
    }
}
