package com.fundpilot.application.service;

import com.fundpilot.application.dto.ScoreResult;
import com.fundpilot.infrastructure.config.ScoringProperties;
import com.fundpilot.infrastructure.persistence.entity.BenchmarkHistoryEntity;
import com.fundpilot.infrastructure.persistence.entity.NavHistoryEntity;
import com.fundpilot.infrastructure.persistence.entity.SignalLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScoringServiceTest {

    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService(new ScoringProperties());
    }

    @Test
    void shouldReturnAvoidWhenHistoryMissing() {
        ScoreResult result = scoringService.calculate(List.of(), List.of(), null);
        assertEquals(SignalLabel.AVOID, result.label());
        assertTrue(result.score().doubleValue() <= 30);
    }

    @Test
    void shouldReturnAvoidWhenHistoryTooShort() {
        List<NavHistoryEntity> nav = createNavSeries(10, 10.0, 0.01);
        ScoreResult result = scoringService.calculate(nav, List.of(), null);
        assertEquals(SignalLabel.AVOID, result.label());
    }

    @Test
    void shouldHandleExtremeDrawdown() {
        List<NavHistoryEntity> nav = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(300);
        for (int i = 0; i < 260; i++) {
            double value = i < 130 ? 10 + i * 0.05 : 16 - (i - 130) * 0.1;
            nav.add(navPoint(start.plusDays(i), value));
        }
        ScoreResult result = scoringService.calculate(nav, List.of(), null);
        assertTrue(result.score().doubleValue() >= 0 && result.score().doubleValue() <= 100);
        assertTrue(result.reasons().stream().anyMatch(r -> r.contains("drawdown")));
    }

    @Test
    void shouldHandleMovingAverageEdgeCaseWithFlatData() {
        List<NavHistoryEntity> nav = createNavSeries(260, 10.0, 0.0);
        ScoreResult result = scoringService.calculate(nav, List.of(), null);
        assertNotNull(result.label());
        assertTrue(result.score().doubleValue() >= 0 && result.score().doubleValue() <= 100);
    }

    @Test
    void shouldHandleBenchmarkAbsentAndPresent() {
        List<NavHistoryEntity> nav = createNavSeries(260, 10.0, 0.015);
        ScoreResult noBenchmark = scoringService.calculate(nav, List.of(), null);

        List<BenchmarkHistoryEntity> benchmark = createBenchmarkSeries(260, 1000.0, 0.009);
        ScoreResult withBenchmark = scoringService.calculate(nav, benchmark, "VNINDEX");

        assertNotNull(noBenchmark.score());
        assertNotNull(withBenchmark.score());
        assertTrue(withBenchmark.reasons().stream().anyMatch(r -> r.contains("benchmark")));
    }

    @Test
    void classifyThresholdsShouldMatchConfig() {
        assertEquals(SignalLabel.STRONG_BUY, scoringService.classify(80));
        assertEquals(SignalLabel.BUY, scoringService.classify(70));
        assertEquals(SignalLabel.HOLD, scoringService.classify(50));
        assertEquals(SignalLabel.AVOID, scoringService.classify(20));
    }

    private List<NavHistoryEntity> createNavSeries(int points, double startValue, double slope) {
        List<NavHistoryEntity> nav = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(points + 10L);
        for (int i = 0; i < points; i++) {
            nav.add(navPoint(start.plusDays(i), startValue + i * slope));
        }
        return nav;
    }

    private List<BenchmarkHistoryEntity> createBenchmarkSeries(int points, double startValue, double slope) {
        List<BenchmarkHistoryEntity> benchmark = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(points + 10L);
        for (int i = 0; i < points; i++) {
            BenchmarkHistoryEntity b = new BenchmarkHistoryEntity();
            b.setId(UUID.randomUUID());
            b.setBenchmarkCode("VNINDEX");
            b.setBenchmarkDate(start.plusDays(i));
            b.setBenchmarkValue(BigDecimal.valueOf(startValue + i * slope));
            benchmark.add(b);
        }
        return benchmark;
    }

    private NavHistoryEntity navPoint(LocalDate date, double value) {
        NavHistoryEntity n = new NavHistoryEntity();
        n.setId(UUID.randomUUID());
        n.setNavDate(date);
        n.setNavValue(BigDecimal.valueOf(value));
        return n;
    }
}
