package com.zeynepates.maisonparfait.backend.identity;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Small in-memory fixed-window limiter for login/forgot-password/
 * resend-verification - single-instance deployment, so no Redis needed.
 * Deliberately separate from User.lockedAt: this is transient and
 * self-clears after the window, lockedAt is a persistent, explicit state
 * only an admin action clears (see docs/identity-module-design.md #10).
 */
@Component
public class RateLimiter {

    private static final Duration STALE_WINDOW_CUTOFF = Duration.ofHours(1);

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Returns true and consumes one attempt if the caller is still under
     * maxAttempts within the current window; returns false (and consumes
     * nothing further) once the limit is hit, until the window rolls over.
     */
    public boolean tryConsume(String key, int maxAttempts, Duration window) {
        Window w = windows.computeIfAbsent(key, k -> new Window());

        synchronized (w) {
            if (Duration.between(w.windowStart, Instant.now()).compareTo(window) > 0) {
                w.windowStart = Instant.now();
                w.count.set(0);
            }
            if (w.count.get() >= maxAttempts) {
                return false;
            }
            w.count.incrementAndGet();
            return true;
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    void evictStaleWindows() {
        Instant cutoff = Instant.now().minus(STALE_WINDOW_CUTOFF);
        windows.entrySet().removeIf(entry -> entry.getValue().windowStart.isBefore(cutoff));
    }

    private static final class Window {
        final AtomicInteger count = new AtomicInteger(0);
        volatile Instant windowStart = Instant.now();
    }
}
