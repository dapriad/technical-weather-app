package com.dapriad.weather.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aemet-budget")
public class AemetBudgetProperties {

    private int maxCallsPerDay = 300;
    private int maxCallsPerMinute = 10;
    private int maxCallsBurst = 5;

    public int getMaxCallsPerDay() {
        return maxCallsPerDay;
    }

    public void setMaxCallsPerDay(int maxCallsPerDay) {
        this.maxCallsPerDay = maxCallsPerDay;
    }

    public int getMaxCallsPerMinute() {
        return maxCallsPerMinute;
    }

    public void setMaxCallsPerMinute(int maxCallsPerMinute) {
        this.maxCallsPerMinute = maxCallsPerMinute;
    }

    public int getMaxCallsBurst() {
        return maxCallsBurst;
    }

    public void setMaxCallsBurst(int maxCallsBurst) {
        this.maxCallsBurst = maxCallsBurst;
    }
}
