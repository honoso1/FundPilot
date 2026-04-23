package com.fundpilot.application.service;

import com.fundpilot.application.dto.ScoreResult;
import com.fundpilot.infrastructure.config.ScoringProperties;
import com.fundpilot.infrastructure.persistence.entity.BenchmarkHistoryEntity;
import com.fundpilot.infrastructure.persistence.entity.NavHistoryEntity;
import com.fundpilot.infrastructure.persistence.entity.SignalLabel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoringService {
    private static final int MIN_HISTORY = 20;

    private final ScoringProperties config;

    public ScoringService(ScoringProperties config) {
        this.config = config;
    }

    public ScoreResult calculate(List<NavHistoryEntity> navAsc, List<BenchmarkHistoryEntity> benchmarkAsc, String benchmarkCode) {
        if (navAsc == null || navAsc.size() < MIN_HISTORY) {
            return new ScoreResult(BigDecimal.valueOf(30), SignalLabel.AVOID,
                    List.of("Insufficient NAV history (<20 points)"), "{}");
        }

        Map<String, Double> metrics = new LinkedHashMap<>();
        List<String> reasons = new ArrayList<>();

        double current = navAsc.getLast().getNavValue().doubleValue();
        OptionalDouble ret1yOpt = return1y(navAsc);
        double ret1y = ret1yOpt.orElse(0);
        metrics.put("return1yPct", round(ret1y));

        double ma60 = movingAverage(navAsc, 60);
        double maDiffPct = percentDiff(current, ma60);
        metrics.put("ma60DiffPct", round(maDiffPct));

        double percentile = rangePercentile(navAsc, 252);
        metrics.put("rangePercentile", round(percentile));

        double maxDrawdown = maxDrawdown(navAsc);
        metrics.put("maxDrawdownPct", round(maxDrawdown));

        OptionalDouble benchmarkExcess = benchmarkExcess(benchmarkAsc, benchmarkCode, ret1y);
        benchmarkExcess.ifPresent(value -> metrics.put("benchmarkExcessPct", round(value)));

        double finalScore = weightedScore(ret1y, maDiffPct, percentile, maxDrawdown, benchmarkExcess);
        BigDecimal roundedScore = BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP);

        reasons.add(maDiffPct < 0
                ? "NAV is %.2f%% below MA60, suggesting accumulation zone".formatted(Math.abs(maDiffPct))
                : "NAV is %.2f%% above MA60, trend support remains positive".formatted(maDiffPct));
        reasons.add("1Y return is %.2f%%".formatted(ret1y));
        reasons.add("Current NAV percentile in recent range is %.1f".formatted(percentile));
        reasons.add("Max drawdown observed is %.2f%%".formatted(maxDrawdown));
        reasons.add(benchmarkExcess.isPresent()
                ? "Fund vs benchmark over 1Y: %.2f%%".formatted(benchmarkExcess.getAsDouble())
                : "Benchmark comparison unavailable; neutral benchmark score applied");

        String metricsJson = metrics.entrySet().stream()
                .map(e -> "\"%s\":%s".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));

        return new ScoreResult(roundedScore, classify(roundedScore.doubleValue()), reasons, metricsJson);
    }

    SignalLabel classify(double score) {
        if (score >= config.getStrongBuyThreshold()) return SignalLabel.STRONG_BUY;
        if (score >= config.getBuyThreshold()) return SignalLabel.BUY;
        if (score >= config.getHoldThreshold()) return SignalLabel.HOLD;
        return SignalLabel.AVOID;
    }

    OptionalDouble return1y(List<NavHistoryEntity> navAsc) {
        if (navAsc.size() < 2) return OptionalDouble.empty();
        int lookback = Math.min(252, navAsc.size() - 1);
        double current = navAsc.getLast().getNavValue().doubleValue();
        double base = navAsc.get(navAsc.size() - 1 - lookback).getNavValue().doubleValue();
        if (base == 0) return OptionalDouble.empty();
        return OptionalDouble.of((current / base - 1) * 100);
    }

    double movingAverage(List<NavHistoryEntity> navAsc, int period) {
        int actual = Math.min(period, navAsc.size());
        return navAsc.subList(navAsc.size() - actual, navAsc.size()).stream()
                .mapToDouble(n -> n.getNavValue().doubleValue())
                .average().orElse(0);
    }

    double rangePercentile(List<NavHistoryEntity> navAsc, int window) {
        int rangeWindow = Math.min(window, navAsc.size());
        List<Double> range = navAsc.subList(navAsc.size() - rangeWindow, navAsc.size())
                .stream().map(v -> v.getNavValue().doubleValue()).toList();
        double current = navAsc.getLast().getNavValue().doubleValue();
        double min = range.stream().min(Double::compareTo).orElse(current);
        double max = range.stream().max(Double::compareTo).orElse(current);
        return max == min ? 50 : ((current - min) / (max - min)) * 100;
    }

    double maxDrawdown(List<NavHistoryEntity> navAsc) {
        double peak = navAsc.getFirst().getNavValue().doubleValue();
        double maxDrawdown = 0;
        for (NavHistoryEntity n : navAsc) {
            double val = n.getNavValue().doubleValue();
            peak = Math.max(peak, val);
            if (peak == 0) continue;
            double dd = (val / peak - 1) * 100;
            maxDrawdown = Math.min(maxDrawdown, dd);
        }
        return maxDrawdown;
    }

    OptionalDouble benchmarkExcess(List<BenchmarkHistoryEntity> benchmarkAsc, String benchmarkCode, double fundRet1y) {
        if (benchmarkCode == null || benchmarkCode.isBlank() || benchmarkAsc == null || benchmarkAsc.size() < 2) {
            return OptionalDouble.empty();
        }
        int lookback = Math.min(252, benchmarkAsc.size() - 1);
        double current = benchmarkAsc.getLast().getBenchmarkValue().doubleValue();
        double base = benchmarkAsc.get(benchmarkAsc.size() - 1 - lookback).getBenchmarkValue().doubleValue();
        if (base == 0) return OptionalDouble.empty();
        double benchmarkRet = (current / base - 1) * 100;
        return OptionalDouble.of(fundRet1y - benchmarkRet);
    }

    double weightedScore(double ret1y, double maDiffPct, double percentile, double maxDrawdown, OptionalDouble benchmarkExcess) {
        double score = 0;
        score += metricReturnScore(ret1y) * config.getWeightReturn1y();
        score += metricMaScore(maDiffPct) * config.getWeightMa();
        score += metricPercentileScore(percentile) * config.getWeightPercentile();
        score += metricDrawdownScore(maxDrawdown) * config.getWeightDrawdown();
        score += benchmarkExcess.isPresent()
                ? metricBenchmarkScore(benchmarkExcess.getAsDouble()) * config.getWeightBenchmark()
                : 50 * config.getWeightBenchmark();
        return clamp(score, 0, 100);
    }

    double metricReturnScore(double ret1y) { return clamp((ret1y + 20) / 50 * 100, 0, 100); }

    double metricMaScore(double maDiffPct) { return clamp((5 - Math.abs(maDiffPct)) / 5 * 100, 0, 100); }

    double metricPercentileScore(double percentile) { return clamp(100 - percentile, 0, 100); }

    double metricDrawdownScore(double maxDrawdown) { return clamp((20 + maxDrawdown) / 20 * 100, 0, 100); }

    double metricBenchmarkScore(double benchmarkExcess) { return clamp((benchmarkExcess + 10) / 20 * 100, 0, 100); }

    private double percentDiff(double current, double base) {
        if (base == 0) return 0;
        return (current / base - 1) * 100;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
