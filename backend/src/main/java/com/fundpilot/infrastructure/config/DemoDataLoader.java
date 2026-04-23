package com.fundpilot.infrastructure.config;

import com.fundpilot.application.service.IngestionService;
import com.fundpilot.infrastructure.persistence.repository.FundRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataLoader implements CommandLineRunner {
    private final FundRepository fundRepository;
    private final IngestionService ingestionService;

    @Value("${app.seed-demo:true}")
    private boolean seedDemo;

    public DemoDataLoader(FundRepository fundRepository, IngestionService ingestionService) {
        this.fundRepository = fundRepository;
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(String... args) {
        if (seedDemo && fundRepository.count() == 0) {
            ingestionService.importMock();
        }
    }
}
