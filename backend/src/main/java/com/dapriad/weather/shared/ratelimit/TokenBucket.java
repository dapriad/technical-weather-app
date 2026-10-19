package com.dapriad.weather.shared.ratelimit;

import java.time.Duration;
import java.util.function.LongSupplier;

public final class TokenBucket {

    private final long capacity;
    private final double refillPerNano;
    private final LongSupplier nanoClock;
    private double tokens;
    private long lastRefillNanos;

    public TokenBucket(long capacity, long refillPerMinute) {
        this(capacity, refillPerMinute, System::nanoTime);
    }

    public TokenBucket(long capacity, long refillPerMinute, LongSupplier nanoClock) {
        this.capacity = capacity;
        this.refillPerNano = (double) refillPerMinute / Duration.ofMinutes(1).toNanos();
        this.nanoClock = nanoClock;
        this.tokens = capacity;
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        long elapsed = now - lastRefillNanos;
        if (elapsed > 0) {
            tokens = Math.min(capacity, tokens + elapsed * refillPerNano);
            lastRefillNanos = now;
        }
    }
}
