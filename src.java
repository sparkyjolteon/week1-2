import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    PriorityQueue<Query> topQueries =
        new PriorityQueue<>(Comparator.comparingInt(q -> q.freq)); // Min-heap
}

class Query {
    String text;
    int freq;
    Query(String t, int f) { text = t; freq = f; }
}

class AutocompleteSystem {

    private final Map<String, Integer> frequencyMap = new HashMap<>();
    private final TrieNode root = new TrieNode();
    private static final int TOP_K = 10;

    public void insertOrUpdate(String query) {
        int freq = frequencyMap.getOrDefault(query, 0) + 1;
        frequencyMap.put(query, freq);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
            addToHeap(node.topQueries, new Query(query, freq));
        }
    }

    private void addToHeap(PriorityQueue<Query> heap, Query q) {
        heap.offer(q);
        if (heap.size() > TOP_K) heap.poll();
    }

    public List<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptyList();
        }

        List<Query> list = new ArrayList<>(node.topQueries);
        list.sort((a,b) -> b.freq - a.freq);

        List<String> result = new ArrayList<>();
        for (Query q : list) result.add(q.text + " (" + q.freq + ")");
        return result;
    }
}

public class AutocompleteApp {
    public static void main(String[] args) {
        AutocompleteSystem ac = new AutocompleteSystem();

        ac.insertOrUpdate("java tutorial");
        ac.insertOrUpdate("javascript");
        ac.insertOrUpdate("java download");
        ac.insertOrUpdate("java tutorial");

        System.out.println(ac.search("jav"));
    }
}
