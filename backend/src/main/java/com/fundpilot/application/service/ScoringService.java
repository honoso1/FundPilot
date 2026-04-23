package com.fundpilot.application.service;

import com.fundpilot.application.dto.ScoreResult;
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

    public ScoreResult calculate(List<NavHistoryEntity> navAsc, List<BenchmarkHistoryEntity> benchmarkAsc, String benchmarkCode) {
        if (navAsc.size() < 20) {
            return new ScoreResult(BigDecimal.valueOf(30), SignalLabel.AVOID, List.of("Insufficient NAV history (<20 points)"), "{}");
        }

        Map<String, Double> metrics = new LinkedHashMap<>();
        List<String> reasons = new ArrayList<>();

        double current = navAsc.get(navAsc.size() - 1).getNavValue().doubleValue();

        int lookback1y = Math.min(252, navAsc.size() - 1);
        double ret1y = 0;
        if (lookback1y > 0) {
            double base = navAsc.get(navAsc.size() - 1 - lookback1y).getNavValue().doubleValue();
            ret1y = (current / base - 1) * 100;
            metrics.put("return1yPct", round(ret1y));
        }

        double ma60 = movingAverage(navAsc, 60);
        double maDiffPct = (current / ma60 - 1) * 100;
        metrics.put("ma60DiffPct", round(maDiffPct));

        int rangeWindow = Math.min(252, navAsc.size());
        List<Double> range = navAsc.subList(navAsc.size() - rangeWindow, navAsc.size())
                .stream().map(v -> v.getNavValue().doubleValue()).toList();
        double min = range.stream().min(Double::compareTo).orElse(current);
        double max = range.stream().max(Double::compareTo).orElse(current);
        double percentile = max == min ? 50 : ((current - min) / (max - min)) * 100;
        metrics.put("rangePercentile", round(percentile));

        double peak = navAsc.get(0).getNavValue().doubleValue();
        double maxDrawdown = 0;
        for (NavHistoryEntity n : navAsc) {
            double val = n.getNavValue().doubleValue();
            peak = Math.max(peak, val);
            double dd = (val / peak - 1) * 100;
            maxDrawdown = Math.min(maxDrawdown, dd);
        }
        metrics.put("maxDrawdownPct", round(maxDrawdown));

        Double benchmarkExcess = null;
        if (benchmarkCode != null && !benchmarkCode.isBlank() && !benchmarkAsc.isEmpty()) {
            int bLookback = Math.min(252, benchmarkAsc.size() - 1);
            if (bLookback > 0) {
                double bCurrent = benchmarkAsc.get(benchmarkAsc.size() - 1).getBenchmarkValue().doubleValue();
                double bBase = benchmarkAsc.get(benchmarkAsc.size() - 1 - bLookback).getBenchmarkValue().doubleValue();
                double bRet = (bCurrent / bBase - 1) * 100;
                benchmarkExcess = ret1y - bRet;
                metrics.put("benchmarkExcessPct", round(benchmarkExcess));
            }
        }

        double score = 0;
        score += clamp((ret1y + 20) / 50 * 100, 0, 100) * 0.35;
        score += clamp((5 - Math.abs(maDiffPct)) / 5 * 100, 0, 100) * 0.25;
        score += clamp((100 - percentile), 0, 100) * 0.15;
        score += clamp((20 + maxDrawdown) / 20 * 100, 0, 100) * 0.15;
        if (benchmarkExcess != null) {
            score += clamp((benchmarkExcess + 10) / 20 * 100, 0, 100) * 0.10;
        } else {
            score += 50 * 0.10;
        }

        if (maDiffPct < 0) reasons.add("NAV is %.2f%% below MA60, suggesting accumulation zone".formatted(Math.abs(maDiffPct)));
        else reasons.add("NAV is %.2f%% above MA60, trend support remains positive".formatted(maDiffPct));

        reasons.add("1Y return is %.2f%%".formatted(ret1y));
        reasons.add("Current NAV percentile in recent range is %.1f".formatted(percentile));
        reasons.add("Max drawdown observed is %.2f%%".formatted(maxDrawdown));
        if (benchmarkExcess != null) {
            reasons.add("Fund vs benchmark over 1Y: %.2f%%".formatted(benchmarkExcess));
        }

        BigDecimal roundedScore = BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
        SignalLabel label = classify(roundedScore.doubleValue());

        String metricsJson = metrics.entrySet().stream()
                .map(e -> "\"%s\":%s".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));

        return new ScoreResult(roundedScore, label, reasons, metricsJson);
    }

    public SignalLabel classify(double score) {
        if (score >= 80) return SignalLabel.STRONG_BUY;
        if (score >= 65) return SignalLabel.BUY;
        if (score >= 45) return SignalLabel.HOLD;
        return SignalLabel.AVOID;
    }

    private double movingAverage(List<NavHistoryEntity> navAsc, int period) {
        int actual = Math.min(period, navAsc.size());
        return navAsc.subList(navAsc.size() - actual, navAsc.size()).stream()
                .mapToDouble(n -> n.getNavValue().doubleValue())
                .average().orElse(navAsc.get(navAsc.size() - 1).getNavValue().doubleValue());
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
