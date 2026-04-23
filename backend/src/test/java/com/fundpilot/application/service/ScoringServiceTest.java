package com.fundpilot.application.service;

import com.fundpilot.application.dto.ScoreResult;
import com.fundpilot.infrastructure.persistence.entity.NavHistoryEntity;
import com.fundpilot.infrastructure.persistence.entity.SignalLabel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService();

    @Test
    void shouldGenerateScoreAndLabelForHealthyTrend() {
        List<NavHistoryEntity> nav = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(300);
        for (int i = 0; i < 260; i++) {
            NavHistoryEntity n = new NavHistoryEntity();
            n.setId(UUID.randomUUID());
            n.setNavDate(start.plusDays(i));
            n.setNavValue(BigDecimal.valueOf(10 + i * 0.02));
            nav.add(n);
        }

        ScoreResult result = scoringService.calculate(nav, List.of(), null);

        assertTrue(result.score().doubleValue() >= 0 && result.score().doubleValue() <= 100);
        assertNotNull(result.label());
        assertFalse(result.reasons().isEmpty());
    }

    @Test
    void classifyThresholdsShouldMatchConfig() {
        assertEquals(SignalLabel.STRONG_BUY, scoringService.classify(80));
        assertEquals(SignalLabel.BUY, scoringService.classify(70));
        assertEquals(SignalLabel.HOLD, scoringService.classify(50));
        assertEquals(SignalLabel.AVOID, scoringService.classify(20));
    }
}
