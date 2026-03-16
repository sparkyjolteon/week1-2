import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

class PageEvent {
    String url;
    String userId;
    String source;

    PageEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class StreamingAnalytics {

    private ConcurrentHashMap<String, AtomicLong> pageViews = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AtomicLong> sourceCounts = new ConcurrentHashMap<>();

    // Process incoming event
    public void processEvent(PageEvent event) {
        // Page views
        pageViews.computeIfAbsent(event.url, k -> new AtomicLong()).incrementAndGet();

        // Unique visitors
        uniqueVisitors
            .computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
            .add(event.userId);

        // Traffic sources
        sourceCounts.computeIfAbsent(event.source, k -> new AtomicLong())
                    .incrementAndGet();
    }

    // Get Top N pages
    private List<String> getTopPages(int n) {
        PriorityQueue<Map.Entry<String, AtomicLong>> pq =
            new PriorityQueue<>(Comparator.comparingLong(e -> e.getValue().get()));

        for (Map.Entry<String, AtomicLong> e : pageViews.entrySet()) {
            pq.offer(e);
            if (pq.size() > n) pq.poll();
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            Map.Entry<String, AtomicLong> e = pq.poll();
            String url = e.getKey();
            long views = e.getValue().get();
            int unique = uniqueVisitors.getOrDefault(url, Set.of()).size();
            result.add(String.format("%s - %,d views (%,d unique)", url, views, unique));
        }
        Collections.reverse(result);
        return result;
    }

    // Dashboard snapshot
    public void getDashboard() {
        System.out.println("\n=== Real-Time Dashboard ===");

        System.out.println("Top Pages:");
        List<String> top = getTopPages(10);
        for (int i = 0; i < top.size(); i++) {
            System.out.println((i + 1) + ". " + top.get(i));
        }

        long totalSource = sourceCounts.values().stream()
                .mapToLong(AtomicLong::get).sum();

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, AtomicLong> e : sourceCounts.entrySet()) {
            double percent = totalSource == 0 ? 0 :
                    (e.getValue().get() * 100.0 / totalSource);
            System.out.printf("%s: %.0f%%%n", e.getKey(), percent);
        }
    }
}

public class RealTimeAnalyticsApp {

    public static void main(String[] args) throws Exception {
        StreamingAnalytics analytics = new StreamingAnalytics();

        // Simulated stream
        analytics.processEvent(new PageEvent("/article/breaking-news", "user_123", "google"));
        analytics.processEvent(new PageEvent("/article/breaking-news", "user_456", "facebook"));
        analytics.processEvent(new PageEvent("/sports/championship", "user_123", "direct"));
        analytics.processEvent(new PageEvent("/article/breaking-news", "user_789", "google"));

        // Auto-refresh dashboard every 5 seconds
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(analytics::getDashboard, 0, 5, TimeUnit.SECONDS);
    }
}
