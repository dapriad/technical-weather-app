package com.dapriad.weather.shared.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class TokenBucketTest {

    @Test
    void shouldConsumeBurstAndRefillAtConfiguredRate() {
        AtomicLong nanoClock = new AtomicLong();
        TokenBucket bucket = new TokenBucket(2, 60, nanoClock::get);

        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();

        nanoClock.addAndGet(Duration.ofSeconds(1).toNanos());
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();

        nanoClock.addAndGet(Duration.ofMinutes(1).toNanos());
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    void shouldNotRefillBeforeTimePasses() {
        AtomicLong nanoClock = new AtomicLong();
        TokenBucket bucket = new TokenBucket(1, 60, nanoClock::get);

        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
    }
}
