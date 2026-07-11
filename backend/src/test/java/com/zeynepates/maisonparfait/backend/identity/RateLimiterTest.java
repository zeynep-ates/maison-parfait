package com.zeynepates.maisonparfait.backend.identity;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    private final RateLimiter rateLimiter = new RateLimiter();

    @Test
    void allowsUpToMaxAttemptsThenBlocks() {
        String key = "test-key-1";

        for (int i = 0; i < 3; i++) {
            assertThat(rateLimiter.tryConsume(key, 3, Duration.ofMinutes(15))).isTrue();
        }

        assertThat(rateLimiter.tryConsume(key, 3, Duration.ofMinutes(15))).isFalse();
    }

    @Test
    void differentKeysAreTrackedIndependently() {
        assertThat(rateLimiter.tryConsume("key-a", 1, Duration.ofMinutes(15))).isTrue();
        assertThat(rateLimiter.tryConsume("key-a", 1, Duration.ofMinutes(15))).isFalse();

        // a different key has its own, unaffected budget
        assertThat(rateLimiter.tryConsume("key-b", 1, Duration.ofMinutes(15))).isTrue();
    }

    @Test
    void windowResetsAfterItExpires() throws InterruptedException {
        String key = "test-key-2";
        Duration shortWindow = Duration.ofMillis(50);

        assertThat(rateLimiter.tryConsume(key, 1, shortWindow)).isTrue();
        assertThat(rateLimiter.tryConsume(key, 1, shortWindow)).isFalse();

        Thread.sleep(100);

        assertThat(rateLimiter.tryConsume(key, 1, shortWindow)).isTrue();
    }
}
