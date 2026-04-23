package com.fundpilot.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scoring")
public class ScoringProperties {
    private int strongBuyThreshold = 80;
    private int buyThreshold = 65;
    private int holdThreshold = 45;

    private double weightReturn1y = 0.35;
    private double weightMa = 0.25;
    private double weightPercentile = 0.15;
    private double weightDrawdown = 0.15;
    private double weightBenchmark = 0.10;

    public int getStrongBuyThreshold() { return strongBuyThreshold; }
    public void setStrongBuyThreshold(int strongBuyThreshold) { this.strongBuyThreshold = strongBuyThreshold; }
    public int getBuyThreshold() { return buyThreshold; }
    public void setBuyThreshold(int buyThreshold) { this.buyThreshold = buyThreshold; }
    public int getHoldThreshold() { return holdThreshold; }
    public void setHoldThreshold(int holdThreshold) { this.holdThreshold = holdThreshold; }
    public double getWeightReturn1y() { return weightReturn1y; }
    public void setWeightReturn1y(double weightReturn1y) { this.weightReturn1y = weightReturn1y; }
    public double getWeightMa() { return weightMa; }
    public void setWeightMa(double weightMa) { this.weightMa = weightMa; }
    public double getWeightPercentile() { return weightPercentile; }
    public void setWeightPercentile(double weightPercentile) { this.weightPercentile = weightPercentile; }
    public double getWeightDrawdown() { return weightDrawdown; }
    public void setWeightDrawdown(double weightDrawdown) { this.weightDrawdown = weightDrawdown; }
    public double getWeightBenchmark() { return weightBenchmark; }
    public void setWeightBenchmark(double weightBenchmark) { this.weightBenchmark = weightBenchmark; }
}
