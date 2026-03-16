import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class UsernameRegistry {

    // username -> userId
    private ConcurrentHashMap<String, Integer> userMap = new ConcurrentHashMap<>();

    // username -> attempt count
    private ConcurrentHashMap<String, Integer> attemptMap = new ConcurrentHashMap<>();

    private int userIdCounter = 1;

    // Register a username
    public boolean register(String username) {
        if (userMap.containsKey(username)) {
            return false;
        }
        userMap.put(username, userIdCounter++);
        return true;
    }

    // Check availability (O(1))
    public boolean checkAvailability(String username) {
        attemptMap.merge(username, 1, Integer::sum);
        return !userMap.containsKey(username);
    }

    // Suggest similar usernames
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String candidate = username + i;
            if (!userMap.containsKey(candidate)) {
                suggestions.add(candidate);
            }
        }

        String dotted = username.replace('_', '.');
        if (!userMap.containsKey(dotted)) {
            suggestions.add(dotted);
        }

        return suggestions;
    }

    // Most attempted username
    public String getMostAttempted() {
        String maxUser = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : attemptMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxUser = entry.getKey();
            }
        }
        return maxUser + " (" + maxCount + " attempts)";
    }
}

public class UsernameSystemApp {

    public static void main(String[] args) {
        UsernameRegistry registry = new UsernameRegistry();

        // Pre-register some users
        registry.register("john_doe");
        registry.register("admin");
        registry.register("player1");

        System.out.println("checkAvailability(\"john_doe\") → " +
                registry.checkAvailability("john_doe"));

        System.out.println("checkAvailability(\"jane_smith\") → " +
                registry.checkAvailability("jane_smith"));

        System.out.println("suggestAlternatives(\"john_doe\") → " +
                registry.suggestAlternatives("john_doe"));

        // Simulate attempts
        for (int i = 0; i < 10543; i++) {
            registry.checkAvailability("admin");
        }

        System.out.println("getMostAttempted() → " +
                registry.getMostAttempted());
    }
}
