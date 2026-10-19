package com.dapriad.weather.infrastructure.adapter.out.aemet;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.LongSupplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dapriad.weather.infrastructure.config.AemetBudgetProperties;
import com.dapriad.weather.shared.ratelimit.TokenBucket;

@Component
public class AemetCallBudget {

    private static final ZoneId SPAIN_ZONE = ZoneId.of("Europe/Madrid");

    private final AemetBudgetProperties properties;
    private final Clock clock;
    private final TokenBucket minuteBucket;
    private LocalDate currentDay;
    private int callsToday;

    @Autowired
    public AemetCallBudget(AemetBudgetProperties properties, Clock clock) {
        this(properties, clock, System::nanoTime);
    }

    AemetCallBudget(AemetBudgetProperties properties, Clock clock, LongSupplier nanoClock) {
        this.properties = properties;
        this.clock = clock;
        this.minuteBucket = new TokenBucket(
                properties.getMaxCallsBurst(),
                properties.getMaxCallsPerMinute(),
                nanoClock);
        this.currentDay = LocalDate.now(clock.withZone(SPAIN_ZONE));
    }

    public synchronized void acquire() {
        LocalDate today = LocalDate.now(clock.withZone(SPAIN_ZONE));
        if (!today.equals(currentDay)) {
            currentDay = today;
            callsToday = 0;
        }

        if (callsToday >= properties.getMaxCallsPerDay()) {
            throw new AemetClientException(
                    AemetClientException.Reason.BUDGET_EXHAUSTED,
                    "Daily AEMET call budget exhausted");
        }

        if (!minuteBucket.tryConsume()) {
            throw new AemetClientException(
                    AemetClientException.Reason.BUDGET_EXHAUSTED,
                    "AEMET per-minute call budget exhausted");
        }

        callsToday++;
    }
}
