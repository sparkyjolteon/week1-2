import java.util.*;

class VideoData {
    String videoId;
    String content;
    VideoData(String id, String c) { videoId = id; content = c; }
}

class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private final int capacity;

    LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > capacity;
    }
}

class VideoCacheSystem {

    private final LRUCache<String, VideoData> L1 =
        new LRUCache<>(10_000);

    private final LRUCache<String, String> L2 =
        new LRUCache<>(100_000); // videoId → SSD path

    private final Map<String, Integer> accessCount = new HashMap<>();

    // Simulated DB
    private final Map<String, VideoData> database = new HashMap<>();

    // Stats
    private int l1Hits=0, l2Hits=0, l3Hits=0;

    public VideoData getVideo(String videoId) {

        // L1
        if (L1.containsKey(videoId)) {
            l1Hits++;
            return L1.get(videoId);
        }

        // L2
        if (L2.containsKey(videoId)) {
            l2Hits++;
            promoteToL1(videoId);
            return L1.get(videoId);
        }

        // L3
        VideoData data = database.get(videoId);
        if (data != null) {
            l3Hits++;
            L2.put(videoId, "/ssd/" + videoId);
            accessCount.put(videoId, 1);
        }
        return data;
    }

    private void promoteToL1(String videoId) {
        int count = accessCount.getOrDefault(videoId, 0) + 1;
        accessCount.put(videoId, count);

        if (count > 3) {
            VideoData data = database.get(videoId);
            L1.put(videoId, data);
        }
    }

    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
    }

    public void printStats() {
        int total = l1Hits + l2Hits + l3Hits;
        System.out.println("L1 Hit Rate: " + percent(l1Hits, total));
        System.out.println("L2 Hit Rate: " + percent(l2Hits, total));
        System.out.println("L3 Hit Rate: " + percent(l3Hits, total));
    }

    private String percent(int x, int total) {
        return total==0 ? "0%" :
            String.format("%.1f%%", x*100.0/total);
    }
}

public class CacheHierarchyApp {
    public static void main(String[] args) {
        VideoCacheSystem cache = new VideoCacheSystem();
        cache.getVideo("video_123");
        cache.printStats();
    }
}
