package com.fundpilot.application.service;

import com.fundpilot.common.exception.NotFoundException;
import com.fundpilot.infrastructure.persistence.entity.FundEntity;
import com.fundpilot.infrastructure.persistence.entity.NavHistoryEntity;
import com.fundpilot.infrastructure.persistence.entity.SignalEntity;
import com.fundpilot.infrastructure.persistence.repository.FundRepository;
import com.fundpilot.infrastructure.persistence.repository.NavHistoryRepository;
import com.fundpilot.infrastructure.persistence.repository.SignalRepository;
import com.fundpilot.interfaces.rest.dto.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class FundQueryService {
    private final FundRepository fundRepository;
    private final NavHistoryRepository navHistoryRepository;
    private final SignalRepository signalRepository;

    public FundQueryService(FundRepository fundRepository, NavHistoryRepository navHistoryRepository, SignalRepository signalRepository) {
        this.fundRepository = fundRepository;
        this.navHistoryRepository = navHistoryRepository;
        this.signalRepository = signalRepository;
    }

    public List<FundSummaryResponse> getFunds() {
        return fundRepository.findAll().stream().map(f -> {
            List<NavHistoryEntity> navDesc = navHistoryRepository.findByFundOrderByNavDateDesc(f);
            BigDecimal latestNav = navDesc.isEmpty() ? null : navDesc.get(0).getNavValue();
            SignalEntity s = signalRepository.findTopByFundOrderBySignalDateDesc(f).orElse(null);
            return new FundSummaryResponse(f.getId(), f.getCode(), f.getName(), latestNav,
                    s != null ? s.getScore() : null, s != null ? s.getLabel() : null);
        }).sorted(Comparator.comparing(FundSummaryResponse::code)).toList();
    }

    public FundDetailResponse getFund(UUID fundId) {
        FundEntity fund = findFund(fundId);
        return new FundDetailResponse(fund.getId(), fund.getCode(), fund.getName(), fund.getBenchmarkCode());
    }

    public List<NavHistoryResponse> getNavHistory(UUID fundId) {
        FundEntity fund = findFund(fundId);
        return navHistoryRepository.findByFundOrderByNavDateAsc(fund).stream()
                .map(n -> new NavHistoryResponse(n.getNavDate(), n.getNavValue())).toList();
    }

    public SignalResponse getLatestSignal(UUID fundId) {
        FundEntity fund = findFund(fundId);
        SignalEntity signal = signalRepository.findTopByFundOrderBySignalDateDesc(fund)
                .orElseThrow(() -> new NotFoundException("No signal found for fund: " + fundId));
        return new SignalResponse(signal.getSignalDate(), signal.getScore(), signal.getLabel(), signal.getReasons(), signal.getMetricsJson());
    }

    private FundEntity findFund(UUID fundId) {
        return fundRepository.findById(fundId).orElseThrow(() -> new NotFoundException("Fund not found: " + fundId));
    }
}
