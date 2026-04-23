package com.fundpilot.application.service;

import com.fundpilot.application.dto.ScoreResult;
import com.fundpilot.common.exception.BadRequestException;
import com.fundpilot.infrastructure.persistence.entity.*;
import com.fundpilot.infrastructure.persistence.repository.BenchmarkHistoryRepository;
import com.fundpilot.infrastructure.persistence.repository.FundRepository;
import com.fundpilot.infrastructure.persistence.repository.NavHistoryRepository;
import com.fundpilot.infrastructure.persistence.repository.SignalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class IngestionService {
    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

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
        log.info("ingestion event=mock_import_start");
        importCsv(generateMockCsv());
        log.info("ingestion event=mock_import_complete");
    }

    @Transactional
    public void importCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new BadRequestException("CSV content must not be empty");
        }

        String[] lines = csv.split("\\R");
        if (lines.length < 2) {
            throw new BadRequestException("CSV must include header and at least one data row");
        }

        int processed = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split(",");
            if (cols.length < 5) {
                throw new BadRequestException("Invalid CSV row at line " + (i + 1) + ": expected at least 5 columns");
            }

            try {
                String fundCode = cols[0].trim();
                String fundName = cols[1].trim();
                LocalDate date = LocalDate.parse(cols[2].trim());
                BigDecimal nav = new BigDecimal(cols[3].trim());
                String benchmarkCode = cols[4].trim();

                FundEntity fund = upsertFund(fundCode, fundName, benchmarkCode);
                upsertNavRecord(fund, date, nav);
                if (!benchmarkCode.isBlank() && cols.length >= 6 && !cols[5].isBlank()) {
                    upsertBenchmarkRecord(benchmarkCode, date, new BigDecimal(cols[5].trim()));
                }
                processed++;
            } catch (Exception ex) {
                throw new BadRequestException("Invalid CSV row at line " + (i + 1) + ": " + ex.getMessage());
            }
        }

        log.info("ingestion event=csv_import_complete processed_rows={}", processed);
        generateSignals();
    }

    @Transactional
    public void generateSignals() {
        List<FundEntity> funds = fundRepository.findAll();
        log.info("scoring event=run_start funds={}", funds.size());
        int generated = 0;

        for (FundEntity fund : funds) {
            List<NavHistoryEntity> nav = navHistoryRepository.findByFundOrderByNavDateAsc(fund);
            if (nav.size() < 20) {
                log.info("scoring event=skip_fund fundCode={} reason=insufficient_history points={}", fund.getCode(), nav.size());
                continue;
            }

            int fromIndex = Math.max(19, nav.size() - 30);
            for (int i = fromIndex; i < nav.size(); i++) {
                List<NavHistoryEntity> navWindow = nav.subList(0, i + 1);
                LocalDate signalDate = navWindow.getLast().getNavDate();
                List<BenchmarkHistoryEntity> benchmark = fund.getBenchmarkCode() == null ? List.of()
                        : benchmarkHistoryRepository.findByBenchmarkCodeAndBenchmarkDateLessThanEqualOrderByBenchmarkDateAsc(
                        fund.getBenchmarkCode(), signalDate);

                ScoreResult result = scoringService.calculate(navWindow, benchmark, fund.getBenchmarkCode());

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
                generated++;
            }

            SignalEntity latest = signalRepository.findTopByFundOrderBySignalDateDesc(fund).orElse(null);
            if (latest != null) {
                log.info("scoring event=fund_scored fundCode={} latestScore={} latestLabel={}",
                        fund.getCode(), latest.getScore(), latest.getLabel());
            }
        }

        log.info("scoring event=run_complete generated_signals={}", generated);
    }

    private FundEntity upsertFund(String fundCode, String fundName, String benchmarkCode) {
        return fundRepository.findByCode(fundCode).map(existing -> {
            existing.setName(fundName);
            if (!benchmarkCode.isBlank()) {
                existing.setBenchmarkCode(benchmarkCode);
            }
            return fundRepository.save(existing);
        }).orElseGet(() -> {
            FundEntity f = new FundEntity();
            f.setId(UUID.randomUUID());
            f.setCode(fundCode);
            f.setName(fundName);
            f.setBenchmarkCode(benchmarkCode.isBlank() ? null : benchmarkCode);
            return fundRepository.save(f);
        });
    }

    private void upsertNavRecord(FundEntity fund, LocalDate date, BigDecimal nav) {
        navHistoryRepository.findByFundAndNavDate(fund, date).ifPresentOrElse(existing -> {
            existing.setNavValue(nav);
            navHistoryRepository.save(existing);
        }, () -> {
            NavHistoryEntity nh = new NavHistoryEntity();
            nh.setId(UUID.randomUUID());
            nh.setFund(fund);
            nh.setNavDate(date);
            nh.setNavValue(nav);
            navHistoryRepository.save(nh);
        });
    }

    private void upsertBenchmarkRecord(String benchmarkCode, LocalDate date, BigDecimal benchmarkValue) {
        benchmarkHistoryRepository.findByBenchmarkCodeAndBenchmarkDate(benchmarkCode, date).ifPresentOrElse(existing -> {
            existing.setBenchmarkValue(benchmarkValue);
            benchmarkHistoryRepository.save(existing);
        }, () -> {
            BenchmarkHistoryEntity b = new BenchmarkHistoryEntity();
            b.setId(UUID.randomUUID());
            b.setBenchmarkCode(benchmarkCode);
            b.setBenchmarkDate(date);
            b.setBenchmarkValue(benchmarkValue);
            benchmarkHistoryRepository.save(b);
        });
    }

    private String generateMockCsv() {
        StringBuilder sb = new StringBuilder("fund_code,fund_name,date,nav,benchmark_code,benchmark_value\n");
        LocalDate start = LocalDate.now().minusDays(420);
        String[][] funds = {
                {"VNFD01", "Vietnam Growth Fund"},
                {"VNFD02", "Vietnam Balanced Fund"},
                {"VNFD03", "Vietnam Dividend Fund"},
                {"VNFD04", "Vietnam Equity Index Fund"},
                {"VNFD05", "Vietnam Income Fund"}
        };

        for (int i = 0; i < 360; i++) {
            LocalDate d = start.plusDays(i);
            double bench = 1000 + i * 0.82 + Math.sin(i / 11.0) * 3;
            for (int f = 0; f < funds.length; f++) {
                double drift = switch (f) {
                    case 0 -> 0.018;
                    case 1 -> 0.013;
                    case 2 -> 0.011;
                    case 3 -> 0.016;
                    default -> 0.009;
                };
                double base = 8.0 + f;
                double nav = base + i * drift + Math.sin(i / (10.0 + f * 2)) * (0.17 + f * 0.03);
                sb.append(funds[f][0]).append(',').append(funds[f][1]).append(',').append(d).append(',')
                        .append(String.format(java.util.Locale.US, "%.4f", nav)).append(",VNINDEX,")
                        .append(String.format(java.util.Locale.US, "%.4f", bench)).append('\n');
            }
        }
        return sb.toString();
    }
}
