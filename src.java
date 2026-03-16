import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class InventorySystem {

    // productId -> stock count
    private ConcurrentHashMap<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    // productId -> waiting users (FIFO)
    private ConcurrentHashMap<String, Queue<Integer>> waitingMap = new ConcurrentHashMap<>();

    // Initialize product
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingMap.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Instant stock check
    public String checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";
        return stock.get() + " units available";
    }

    // Purchase operation (thread-safe, O(1))
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";

        while (true) {
            int current = stock.get();
            if (current <= 0) {
                Queue<Integer> queue = waitingMap.get(productId);
                queue.add(userId);
                return "Added to waiting list, position #" + queue.size();
            }
            if (stock.compareAndSet(current, current - 1)) {
                return "Success, " + (current - 1) + " units remaining";
            }
        }
    }

    // Waiting list size
    public int getWaitingCount(String productId) {
        return waitingMap.get(productId).size();
    }
}

public class FlashSaleApp {

    public static void main(String[] args) {
        InventorySystem system = new InventorySystem();

        system.addProduct("IPHONE15_256GB", 100);

        System.out.println("checkStock(\"IPHONE15_256GB\") → " +
                system.checkStock("IPHONE15_256GB"));

        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=12345) → " +
                system.purchaseItem("IPHONE15_256GB", 12345));

        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=67890) → " +
                system.purchaseItem("IPHONE15_256GB", 67890));

        // Simulate stock exhaustion
        for (int i = 0; i < 98; i++) {
            system.purchaseItem("IPHONE15_256GB", i);
        }

        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=99999) → " +
                system.purchaseItem("IPHONE15_256GB", 99999));
    }
}
