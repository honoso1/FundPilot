package com.fundpilot.application.service;

import com.fundpilot.application.dto.ScoreResult;
import com.fundpilot.infrastructure.persistence.entity.*;
import com.fundpilot.infrastructure.persistence.repository.BenchmarkHistoryRepository;
import com.fundpilot.infrastructure.persistence.repository.FundRepository;
import com.fundpilot.infrastructure.persistence.repository.NavHistoryRepository;
import com.fundpilot.infrastructure.persistence.repository.SignalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class IngestionService {
    private final FundRepository fundRepository;
    private final NavHistoryRepository navHistoryRepository;
    private final BenchmarkHistoryRepository benchmarkHistoryRepository;
    private final SignalRepository signalRepository;
    private final ScoringService scoringService;

    public IngestionService(FundRepository fundRepository,
                            NavHistoryRepository navHistoryRepository,
                            BenchmarkHistoryRepository benchmarkHistoryRepository,
                            SignalRepository signalRepository,
                            ScoringService scoringService) {
        this.fundRepository = fundRepository;
        this.navHistoryRepository = navHistoryRepository;
        this.benchmarkHistoryRepository = benchmarkHistoryRepository;
        this.signalRepository = signalRepository;
        this.scoringService = scoringService;
    }

    @Transactional
    public void importMock() {
        importCsv(generateMockCsv());
    }

    @Transactional
    public void importCsv(String csv) {
        String[] lines = csv.split("\\R");
        if (lines.length < 2) return;

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split(",");
            if (cols.length < 5) continue;

            String fundCode = cols[0].trim();
            String fundName = cols[1].trim();
            LocalDate date = LocalDate.parse(cols[2].trim());
            BigDecimal nav = new BigDecimal(cols[3].trim());
            String benchmarkCode = cols[4].trim();

            FundEntity fund = fundRepository.findByCode(fundCode).orElseGet(() -> {
                FundEntity f = new FundEntity();
                f.setId(UUID.randomUUID());
                f.setCode(fundCode);
                f.setName(fundName);
                f.setBenchmarkCode(benchmarkCode.isBlank() ? null : benchmarkCode);
                return fundRepository.save(f);
            });

            navHistoryRepository.findByFundAndNavDate(fund, date).orElseGet(() -> {
                NavHistoryEntity nh = new NavHistoryEntity();
                nh.setId(UUID.randomUUID());
                nh.setFund(fund);
                nh.setNavDate(date);
                nh.setNavValue(nav);
                return navHistoryRepository.save(nh);
            });

            if (!benchmarkCode.isBlank() && cols.length >= 6 && !cols[5].isBlank()) {
                BigDecimal benchmarkValue = new BigDecimal(cols[5].trim());
                benchmarkHistoryRepository.findByBenchmarkCodeAndBenchmarkDate(benchmarkCode, date).orElseGet(() -> {
                    BenchmarkHistoryEntity b = new BenchmarkHistoryEntity();
                    b.setId(UUID.randomUUID());
                    b.setBenchmarkCode(benchmarkCode);
                    b.setBenchmarkDate(date);
                    b.setBenchmarkValue(benchmarkValue);
                    return benchmarkHistoryRepository.save(b);
                });
            }
        }

        generateSignals();
    }

    @Transactional
    public void generateSignals() {
        List<FundEntity> funds = fundRepository.findAll();
        for (FundEntity fund : funds) {
            List<NavHistoryEntity> nav = navHistoryRepository.findByFundOrderByNavDateAsc(fund);
            if (nav.size() < 20) continue;

            List<BenchmarkHistoryEntity> benchmark = fund.getBenchmarkCode() == null ? List.of()
                    : benchmarkHistoryRepository.findByBenchmarkCodeOrderByBenchmarkDateAsc(fund.getBenchmarkCode());
            ScoreResult result = scoringService.calculate(nav, benchmark, fund.getBenchmarkCode());

            LocalDate signalDate = nav.get(nav.size() - 1).getNavDate();
            SignalEntity signal = signalRepository.findByFundAndSignalDate(fund, signalDate).orElseGet(SignalEntity::new);
            if (signal.getId() == null) {
                signal.setId(UUID.randomUUID());
                signal.setFund(fund);
                signal.setSignalDate(signalDate);
            }
            signal.setScore(result.score());
            signal.setLabel(result.label());
            signal.setReasons(String.join(" | ", result.reasons()));
            signal.setMetricsJson(result.metricsJson());
            signalRepository.save(signal);
        }
    }

    private String generateMockCsv() {
        StringBuilder sb = new StringBuilder("fund_code,fund_name,date,nav,benchmark_code,benchmark_value\n");
        LocalDate start = LocalDate.now().minusDays(280);
        for (int i = 0; i < 260; i++) {
            LocalDate d = start.plusDays(i);
            double a = 10 + i * 0.015 + Math.sin(i / 14.0) * 0.2;
            double b = 8 + i * 0.01 + Math.cos(i / 12.0) * 0.15;
            double bench = 1000 + i * 0.9 + Math.sin(i / 9.0) * 3;
            sb.append("VNFD01,Vietnam Growth Fund,").append(d).append(",")
                    .append(String.format(java.util.Locale.US, "%.4f", a)).append(",VNINDEX,")
                    .append(String.format(java.util.Locale.US, "%.4f", bench)).append("\n");
            sb.append("VNFD02,Vietnam Balanced Fund,").append(d).append(",")
                    .append(String.format(java.util.Locale.US, "%.4f", b)).append(",VNINDEX,")
                    .append(String.format(java.util.Locale.US, "%.4f", bench)).append("\n");
        }
        return sb.toString();
    }
}
