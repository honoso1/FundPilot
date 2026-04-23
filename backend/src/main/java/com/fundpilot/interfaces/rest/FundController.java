package com.fundpilot.interfaces.rest;

import com.fundpilot.application.service.FundQueryService;
import com.fundpilot.interfaces.rest.dto.FundDetailResponse;
import com.fundpilot.interfaces.rest.dto.FundSummaryResponse;
import com.fundpilot.interfaces.rest.dto.NavHistoryResponse;
import com.fundpilot.interfaces.rest.dto.SignalResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/funds")
@CrossOrigin(origins = "*")
public class FundController {
    private final FundQueryService fundQueryService;

    public FundController(FundQueryService fundQueryService) {
        this.fundQueryService = fundQueryService;
    }

    @GetMapping
    public List<FundSummaryResponse> getFunds() {
        return fundQueryService.getFunds();
    }

    @GetMapping("/{id}")
    public FundDetailResponse getFund(@PathVariable UUID id) {
        return fundQueryService.getFund(id);
    }

    @GetMapping("/{id}/nav-history")
    public List<NavHistoryResponse> getNavHistory(@PathVariable UUID id) {
        return fundQueryService.getNavHistory(id);
    }

    @GetMapping("/{id}/latest-signal")
    public SignalResponse getLatestSignal(@PathVariable UUID id) {
        return fundQueryService.getLatestSignal(id);
    }
}
