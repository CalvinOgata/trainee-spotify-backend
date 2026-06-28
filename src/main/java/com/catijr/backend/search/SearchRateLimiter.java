package com.catijr.backend.search;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter token-bucket por IP, sem dependências externas (o endpoint é
 * chamado a cada tecla digitada). Capacidade = rajada permitida; recarga de
 * {@code RATE_PER_SEC} tokens/segundo. O mapa é podado quando cresce demais para
 * limitar o consumo de memória sob "IP spraying".
 */
@Component
public class SearchRateLimiter {

    private static final int RATE_PER_SEC = 30;
    private static final long REFILL_INTERVAL_NANOS = 1_000_000_000L / RATE_PER_SEC;
    private static final int MAX_BUCKETS = 50_000;
    private static final long IDLE_EVICT_NANOS = 60_000_000_000L; // 60s

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * @return true se a requisição pode prosseguir; false se o limite foi excedido
     */
    public boolean tryAcquire(String key) {
        if (buckets.size() > MAX_BUCKETS) {
            prune();
        }
        return buckets.computeIfAbsent(key, k -> new Bucket()).tryConsume();
    }

    private void prune() {
        long now = System.nanoTime();
        buckets.forEach((k, b) -> {
            if (now - b.lastSeenNanos() > IDLE_EVICT_NANOS) {
                buckets.remove(k, b);
            }
        });
    }

    private static final class Bucket {
        private double tokens = RATE_PER_SEC;
        private long lastRefillNanos = System.nanoTime();
        private volatile long lastSeen = System.nanoTime();

        synchronized boolean tryConsume() {
            long now = System.nanoTime();
            lastSeen = now;

            long elapsed = now - lastRefillNanos;
            if (elapsed > 0) {
                tokens = Math.min(RATE_PER_SEC, tokens + (double) elapsed / REFILL_INTERVAL_NANOS);
                lastRefillNanos = now;
            }
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        long lastSeenNanos() {
            return lastSeen;
        }
    }
}
