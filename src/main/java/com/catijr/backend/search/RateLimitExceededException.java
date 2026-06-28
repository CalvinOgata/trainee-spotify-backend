package com.catijr.backend.search;

/**
 * Lançada quando um cliente (por IP) ultrapassa o limite de requisições.
 * Carrega o valor sugerido para o header Retry-After.
 */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
