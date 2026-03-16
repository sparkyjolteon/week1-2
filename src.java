import java.time.*;
import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String accountId;
    LocalDateTime time;

    Transaction(int id, int amount, String merchant,
                String accountId, LocalDateTime time) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.accountId = accountId;
        this.time = time;
    }
}

class FraudAnalyzer {

    // Two-Sum
    public List<int[]> findTwoSum(List<Transaction> txns, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : txns) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum within time window
    public List<int[]> findTwoSumWithWindow(List<Transaction> txns,
                                            int target,
                                            Duration window) {
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> filtered = new ArrayList<>();

        for (Transaction t : txns) {
            if (Duration.between(t.time, now).compareTo(window) <= 0)
                filtered.add(t);
        }
        return findTwoSum(filtered, target);
    }

    // Duplicate detection
    public void detectDuplicates(List<Transaction> txns) {
        Map<String, Set<String>> map = new HashMap<>();

        for (Transaction t : txns) {
            String key = t.amount + "|" + t.merchant;
            map.putIfAbsent(key, new HashSet<>());
            map.get(key).add(t.accountId);
        }

        for (String key : map.keySet()) {
            if (map.get(key).size() > 1) {
                System.out.println("Duplicate Suspicious: " + key +
                        " Accounts: " + map.get(key));
            }
        }
    }
}

public class FraudDetectionApp {
    public static void main(String[] args) {
        List<Transaction> txns = List.of(
            new Transaction(1, 500, "Store A", "acc1",
                LocalDateTime.now().minusMinutes(30)),
            new Transaction(2, 300, "Store B", "acc2",
                LocalDateTime.now().minusMinutes(20)),
            new Transaction(3, 200, "Store C", "acc3",
                LocalDateTime.now().minusMinutes(10))
        );

        FraudAnalyzer analyzer = new FraudAnalyzer();

        System.out.println("Two Sum:");
        for (int[] pair : analyzer.findTwoSum(txns, 500)) {
            System.out.println(pair[0] + " & " + pair[1]);
        }

        System.out.println("\nDuplicates:");
        analyzer.detectDuplicates(txns);
    }
}
