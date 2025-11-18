package com.saumyabot.discord_bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeetCodeService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode fetchProfile(String username) {

        String body = """
{
  "query":"query userData($username: String!) { matchedUser(username: $username) { username profile { realName ranking } submitStatsGlobal { acSubmissionNum { difficulty count } } userCalendar { streak } } userContestRanking(username: $username) { rating globalRanking topPercentage } recentSubmissionList(username: $username) { title titleSlug statusDisplay timestamp } }",
  "variables":{"username":"%s"}
}
""".formatted(username);

        try {
            // STEP 1 → GET request to retrieve CSRF cookie
            HttpRequest getReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/graphql"))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "https://leetcode.com/")
                    .header("Origin", "https://leetcode.com")
                    .GET()
                    .build();

            HttpResponse<String> getRes =
                    client.send(getReq, HttpResponse.BodyHandlers.ofString());


            String cookies = getRes.headers().firstValue("set-cookie").orElse("");
            String csrftoken = "";

            for (String c : cookies.split(";")) {
                if (c.trim().startsWith("csrftoken=")) {
                    csrftoken = c.trim().substring("csrftoken=".length());
                }
            }

            System.out.println("CSRF TOKEN = " + csrftoken);


            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/graphql"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "https://leetcode.com/")
                    .header("Origin", "https://leetcode.com")
                    .header("x-csrftoken", csrftoken)
                    .header("Cookie", "csrftoken=" + csrftoken)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> postRes =
                    client.send(postReq, HttpResponse.BodyHandlers.ofString());

            System.out.println("GraphQL Response:");
            System.out.println(postRes.body());


            JsonNode json = mapper.readTree(postRes.body());

            return json.get("data");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode fetchRandomProblemByDifficulty(String difficulty) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/api/problems/all/"))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            JsonNode arr = root.path("stat_status_pairs");

            List<JsonNode> list = new ArrayList<>();

            for (JsonNode p : arr) {
                int diff = p.path("difficulty").path("level").asInt(); // 1, 2, 3
                String slug = p.path("stat").path("question__title_slug").asText();

                if (slug == null || slug.isEmpty()) continue;

                if (difficulty.equalsIgnoreCase("easy") && diff == 1) list.add(p);
                if (difficulty.equalsIgnoreCase("medium") && diff == 2) list.add(p);
                if (difficulty.equalsIgnoreCase("hard") && diff == 3) list.add(p);
            }

            if (list.isEmpty()) return null;

            JsonNode chosen = list.get((int)(Math.random() * list.size()));


            ObjectNode node = mapper.createObjectNode();
            node.put("titleSlug", chosen.path("stat").path("question__title_slug").asText());
            node.put("difficulty", difficulty.toUpperCase());

            return node;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public JsonNode fetchRecentSubmissionList(String username) {
        String body = """
    {
      "query":"query recentSubs($username:String!){ recentSubmissionList(username:$username) { title titleSlug statusDisplay timestamp } }",
      "variables":{"username":"%s"}
    }
    """.formatted(username);

        try {
            // get csrftoken from homepage
            HttpRequest getReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/"))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET().build();

            HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
            String cookies = getRes.headers().firstValue("set-cookie").orElse("");
            String csrftoken = "";
            for (String c : cookies.split(";")) {
                if (c.trim().startsWith("csrftoken=")) {
                    csrftoken = c.trim().substring("csrftoken=".length());
                }
            }

            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/graphql"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "https://leetcode.com/")
                    .header("Origin", "https://leetcode.com")
                    .header("x-csrftoken", csrftoken)
                    .header("Cookie", "csrftoken=" + csrftoken)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(postRes.body());
            return json.path("data").path("recentSubmissionList");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
