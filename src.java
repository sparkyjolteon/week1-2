import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class TokenBucket {
    private final long maxTokens;
    private final double refillRatePerSec;
    private double tokens;
    private long lastRefillTime;

    public TokenBucket(long maxTokens, long refillPerHour) {
        this.maxTokens = maxTokens;
        this.refillRatePerSec = refillPerHour / 3600.0;
        this.tokens = maxTokens;
        this.lastRefillTime = System.nanoTime();
    }

    private void refill() {
        long now = System.nanoTime();
        double seconds = (now - lastRefillTime) / 1_000_000_000.0;
        double refill = seconds * refillRatePerSec;
        tokens = Math.min(maxTokens, tokens + refill);
        lastRefillTime = now;
    }

    public synchronized boolean allowRequest() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    public synchronized long getRemainingTokens() {
        refill();
        return (long) tokens;
    }

    public long getMaxTokens() {
        return maxTokens;
    }
}

class RateLimiter {

    private final ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();
    private static final long MAX_REQUESTS = 1000;

    public boolean checkRateLimit(String clientId) {
        TokenBucket bucket = clientBuckets.computeIfAbsent(
                clientId,
                id -> new TokenBucket(MAX_REQUESTS, MAX_REQUESTS)
        );

        boolean allowed = bucket.allowRequest();
        long remaining = bucket.getRemainingTokens();

        if (allowed) {
            System.out.println("Allowed (" + remaining + " requests remaining)");
        } else {
            long retryAfter = (long)((1.0 / (MAX_REQUESTS / 3600.0)) * (1_000)); // ms approx
            System.out.println("Denied (0 requests remaining, retry after ~" + retryAfter + " ms)");
        }
        return allowed;
    }

    public void getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) {
            System.out.println("No usage yet.");
            return;
        }
        long remaining = bucket.getRemainingTokens();
        long used = bucket.getMaxTokens() - remaining;

        System.out.println("{used: " + used +
                ", limit: " + bucket.getMaxTokens() +
                ", remaining: " + remaining + "}");
    }
}

public class ApiRateLimiterApp {
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        for (int i = 0; i < 5; i++) {
            limiter.checkRateLimit(client);
        }

        limiter.getRateLimitStatus(client);
    }
}
