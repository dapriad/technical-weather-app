package com.dapriad.weather.infrastructure.adapter.out.aemet;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dapriad.weather.infrastructure.config.AemetBudgetProperties;

class AemetCallBudgetTest {

    private AemetBudgetProperties properties;
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        properties = new AemetBudgetProperties();
        clock = new MutableClock(Instant.parse("2026-10-19T10:00:00Z"), ZoneOffset.UTC);
    }

    @Test
    void shouldRejectCallsAfterDailyLimitAndResetNextDay() {
        properties.setMaxCallsPerDay(2);
        properties.setMaxCallsPerMinute(100);
        properties.setMaxCallsBurst(100);
        AemetCallBudget budget = new AemetCallBudget(properties, clock, this::nanoTime);

        budget.acquire();
        budget.acquire();

        assertThatThrownBy(budget::acquire)
                .isInstanceOf(AemetClientException.class)
                .hasFieldOrPropertyWithValue("reason", AemetClientException.Reason.BUDGET_EXHAUSTED);

        clock.advance(Duration.ofDays(1));

        assertThatCode(budget::acquire).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCallsAfterPerMinuteLimitAndRefill() {
        properties.setMaxCallsPerDay(100);
        properties.setMaxCallsPerMinute(60);
        properties.setMaxCallsBurst(1);
        AemetCallBudget budget = new AemetCallBudget(properties, clock, this::nanoTime);

        budget.acquire();

        assertThatThrownBy(budget::acquire)
                .isInstanceOf(AemetClientException.class)
                .hasFieldOrPropertyWithValue("reason", AemetClientException.Reason.BUDGET_EXHAUSTED);

        clock.advance(Duration.ofSeconds(1));

        assertThatCode(budget::acquire).doesNotThrowAnyException();
    }

    private long nanoTime() {
        return clock.millis() * 1_000_000L;
    }

    private static final class MutableClock extends Clock {

        private final AtomicReference<Instant> instant;
        private final ZoneId zone;

        private MutableClock(Instant instant, ZoneId zone) {
            this(new AtomicReference<>(instant), zone);
        }

        private MutableClock(AtomicReference<Instant> instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void advance(Duration duration) {
            instant.updateAndGet(current -> current.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
