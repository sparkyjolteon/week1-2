import java.util.*;

class PlagiarismDetector {

    private static final int N = 5; // n-gram size

    // ngram -> set of document IDs
    private Map<String, Set<String>> ngramIndex = new HashMap<>();

    // documentId -> list of ngrams
    private Map<String, List<String>> documentNgrams = new HashMap<>();

    // Normalize text
    private String normalize(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9 ]", "");
    }

    // Generate n-grams
    private List<String> generateNgrams(String text) {
        String[] words = normalize(text).split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }
        return ngrams;
    }

    // Add document to index
    public void indexDocument(String docId, String content) {
        List<String> ngrams = generateNgrams(content);
        documentNgrams.put(docId, ngrams);

        for (String ngram : ngrams) {
            ngramIndex
                .computeIfAbsent(ngram, k -> new HashSet<>())
                .add(docId);
        }
    }

    // Analyze new document
    public void analyzeDocument(String docId, String content) {
        List<String> ngrams = generateNgrams(content);
        System.out.println("Extracted " + ngrams.size() + " n-grams");

        Map<String, Integer> matchCounts = new HashMap<>();

        for (String ngram : ngrams) {
            Set<String> docs = ngramIndex.get(ngram);
            if (docs != null) {
                for (String d : docs) {
                    matchCounts.merge(d, 1, Integer::sum);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String otherDoc = entry.getKey();
            int matches = entry.getValue();
            double similarity = (matches * 100.0) / ngrams.size();

            System.out.printf(
                "Found %d matching n-grams with \"%s\"%nSimilarity: %.1f%% %s%n",
                matches,
                otherDoc,
                similarity,
                similarity > 60 ? "(PLAGIARISM DETECTED)" :
                similarity > 15 ? "(suspicious)" : ""
            );
        }
    }
}

public class PlagiarismDetectionApp {

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Index existing essays
        detector.indexDocument("essay_089.txt",
            "Data structures and algorithms are fundamental concepts in computer science");

        detector.indexDocument("essay_092.txt",
            "Data structures and algorithms are fundamental concepts in computer science and engineering");

        // Analyze new essay
        detector.analyzeDocument("essay_123.txt",
            "Data structures and algorithms are fundamental concepts in computer science");
    }
}
