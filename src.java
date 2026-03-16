import java.util.*;
import java.util.concurrent.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long timestamp;
    long expiryTime;

    DNSEntry(String domain, String ipAddress, long ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.timestamp = System.currentTimeMillis();
        this.expiryTime = timestamp + ttlSeconds * 1000;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

class DNSCache {

    private final int MAX_SIZE = 5;

    // LRU cache using accessOrder=true
    private LinkedHashMap<String, DNSEntry> cache =
        new LinkedHashMap<>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_SIZE;
            }
        };

    private long hits = 0;
    private long misses = 0;
    private long totalLookupTimeNs = 0;

    public DNSCache() {
        startCleanupTask();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {
        long start = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null) {
            if (!entry.isExpired()) {
                hits++;
                totalLookupTimeNs += System.nanoTime() - start;
                return "Cache HIT → " + entry.ipAddress;
            } else {
                cache.remove(domain);
            }
        }

        // Cache miss → query upstream
        misses++;
        String ip = queryUpstreamDNS(domain);
        cache.put(domain, new DNSEntry(domain, ip, 300));

        totalLookupTimeNs += System.nanoTime() - start;
        return "Cache MISS → Query upstream → " + ip + " (TTL: 300s)";
    }

    // Simulated upstream DNS resolver
    private String queryUpstreamDNS(String domain) {
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        return "172.217.14." + new Random().nextInt(255);
    }

    // Background cleanup
    private void startCleanupTask() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                while (it.hasNext()) {
                    if (it.next().getValue().isExpired()) {
                        it.remove();
                    }
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    // Statistics
    public String getCacheStats() {
        long total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);
        double avgMs = total == 0 ? 0 : (totalLookupTimeNs / 1_000_000.0 / total);

        return String.format("Hit Rate: %.1f%%, Avg Lookup Time: %.2fms", hitRate, avgMs);
    }
}

public class DNSCacheApp {

    public static void main(String[] args) throws Exception {
        DNSCache cache = new DNSCache();

        System.out.println(cache.resolve("google.com"));
        System.out.println(cache.resolve("google.com"));

        Thread.sleep(1000);

        System.out.println(cache.resolve("openai.com"));
        System.out.println(cache.resolve("google.com"));

        System.out.println("getCacheStats() → " + cache.getCacheStats());
    }
}
