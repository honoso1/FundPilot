# Scoring Formula Design (0-100)

## Metric Normalization
Each metric is transformed to 0-100 sub-score.

1. **1Y Return Score**
   - Higher is better with cap range (e.g., -20% to +30%).
2. **3Y Return Score**
   - Optional if enough history; if missing, redistribute weight proportionally.
3. **Max Drawdown Score**
   - Smaller drawdown is better; accumulation zone bonus if moderate drawdown (not extreme crash).
4. **Volatility Score**
   - Lower annualized volatility gets higher score.
5. **Moving Average Position Score**
   - Use current NAV deltas to MA20/60/120.
   - Blend with bias toward MA60 + MA120 for long-term accumulation.
6. **1Y Range Percentile Score**
   - Lower percentile can indicate attractive accumulation, but penalize if too weak unless trend confirms.
7. **Benchmark Outperformance Score**
   - Positive 1Y excess return raises score.

## Weighted Composite
`finalScore = Σ(weight_i * subScore_i) / Σ(activeWeight_i)`

Default weights (sum 100):
- return_1y: 22
- return_3y: 16
- drawdown: 14
- volatility: 12
- ma_position: 16
- percentile: 10
- benchmark: 10

## Classification
- `STRONG_BUY`: score >= 80
- `BUY`: 65 <= score < 80
- `HOLD`: 45 <= score < 65
- `AVOID`: score < 45

## Explainability Rules
For each signal, include top reasons based on impactful factors:
- MA reason example: "NAV is 6.2% below MA60, suggesting accumulation zone"
- Benchmark reason example: "Fund outperformed benchmark by 3.8% over 1Y"
- Risk reason example: "Max drawdown 11.4% is within preferred band"

Store reasons as ordered JSON array with machine-friendly reason codes + human-readable message.
