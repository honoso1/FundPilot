package com.fundpilot.interfaces.rest;

import com.fundpilot.application.service.FundQueryService;
import com.fundpilot.common.api.ApiResponse;
import com.fundpilot.common.api.PagedResponse;
import com.fundpilot.interfaces.rest.dto.FundDetailResponse;
import com.fundpilot.interfaces.rest.dto.FundSummaryResponse;
import com.fundpilot.interfaces.rest.dto.NavHistoryResponse;
import com.fundpilot.interfaces.rest.dto.SignalResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/funds")
@CrossOrigin(origins = "*")
@Validated
public class FundController {
    private final FundQueryService fundQueryService;

    public FundController(FundQueryService fundQueryService) {
        this.fundQueryService = fundQueryService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<FundSummaryResponse>> getFunds(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(fundQueryService.getFunds(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<FundDetailResponse> getFund(@PathVariable UUID id) {
        return ApiResponse.ok(fundQueryService.getFund(id));
    }

    @GetMapping("/{id}/nav-history")
    public ApiResponse<List<NavHistoryResponse>> getNavHistory(@PathVariable UUID id) {
        return ApiResponse.ok(fundQueryService.getNavHistory(id));
    }

    @GetMapping("/{id}/latest-signal")
    public ApiResponse<SignalResponse> getLatestSignal(@PathVariable UUID id) {
        return ApiResponse.ok(fundQueryService.getLatestSignal(id));
    }
}
