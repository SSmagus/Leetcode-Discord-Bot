package com.saumyabot.discord_bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saumyabot.discord_bot.model.Problem;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProblemService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private List<Problem> cachedProblems = new ArrayList<>();
    private long lastRefresh = 0;

    private static final long REFRESH_MS = 24 * 60 * 60 * 1000;

    private static final Set<String> DSA_TAGS = Set.of(
            "Array", "String", "Hash Table", "Math", "Two Pointers",
            "Dynamic Programming", "Binary Search", "Tree",
            "Depth-First Search", "Breadth-First Search", "Graph",
            "Greedy", "Heap (Priority Queue)", "Stack", "Queue",
            "Linked List", "Backtracking", "Trie", "Divide and Conquer",
            "Bit Manipulation", "Sliding Window", "Union Find",
            "Prefix Sum", "Memoization", "Counting",
            "Binary Indexed Tree", "Segment Tree",
            "Monotonic Stack", "Matrix", "Recursion"
    );

    private synchronized void refreshProblems() {
        long now = System.currentTimeMillis();
        if (!cachedProblems.isEmpty() && now - lastRefresh < REFRESH_MS) return;

        List<Problem> result = new ArrayList<>();

        int skip = 0;
        int batch = 100;

        try {
            while (true) {
                String queryJson = """
                {
                  "query": "query problemsetQuestionList($skip:Int,$limit:Int){problemsetQuestionList:questionList(categorySlug:\\"\\",skip:$skip,limit:$limit,filters:{}){questions:data{title titleSlug difficulty isPaidOnly topicTags{name}}}}",
                  "variables": { "skip": %d, "limit": %d }
                }
                """.formatted(skip, batch);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("https://leetcode.com/graphql/"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(queryJson))
                        .build();

                HttpResponse<String> resp =
                        http.send(req, HttpResponse.BodyHandlers.ofString());

                JsonNode root = mapper.readTree(resp.body());
                JsonNode questions =
                        root.path("data").path("problemsetQuestionList").path("questions");

                if (questions.isEmpty()) break;

                for (JsonNode q : questions) {
                    if (q.path("isPaidOnly").asBoolean()) continue;

                    boolean isDSA = false;
                    for (JsonNode t : q.path("topicTags")) {
                        if (DSA_TAGS.contains(t.path("name").asText())) {
                            isDSA = true;
                            break;
                        }
                    }
                    if (!isDSA) continue;

                    String slug = q.path("titleSlug").asText();
                    String title = q.path("title").asText();
                    int diff = switch (q.path("difficulty").asText()) {
                        case "Easy" -> 1;
                        case "Medium" -> 2;
                        case "Hard" -> 3;
                        default -> 2;
                    };

                    result.add(new Problem(slug, title, diff));
                }

                skip += batch;
            }

            cachedProblems = result;
            lastRefresh = now;

            System.out.println("Loaded DSA problems: " + cachedProblems.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Problem getRandomProblem(int difficulty) {
        refreshProblems();

        List<Problem> filtered =
                cachedProblems.stream()
                        .filter(p -> p.getDifficulty() == difficulty)
                        .toList();

        if (filtered.isEmpty()) return null;

        return filtered.get(ThreadLocalRandom.current().nextInt(filtered.size()));
    }
}
