package com.saumyabot.discord_bot.service;

import com.saumyabot.discord_bot.model.User;
import com.saumyabot.discord_bot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class VerificationService {
    private final UserRepository userRepository;

    private final Map<String, ScheduledFuture<?>> pendingExpiry= new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);

    private final Duration TTL= Duration.ofMinutes(5);

    public VerificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateCodeAndSave(String discordId, String leetId){
        String code= makeCode();
        Instant expiresAt= Instant.now().plus(TTL);

        User user= userRepository.findByDiscordId(discordId).orElseGet(User::new);
        user.setDiscordId(discordId);
        user.setVerificationCode(code);
        user.setVerified(false);
        user.setVerificationExpiresAt(expiresAt);
        user.setLeetcodeId(leetId);

        userRepository.save(user);

        ScheduledFuture<?> previous= pendingExpiry.remove(discordId);
        if(previous!=null) previous.cancel(false);

        ScheduledFuture<?> future= scheduler.schedule(
                ()-> expire(discordId),
                TTL.toMillis(),
                TimeUnit.MILLISECONDS
        );

        pendingExpiry.put(discordId, future);

        return code;
    }

    private void expire(String discordId) {
        userRepository.findByDiscordId(discordId).ifPresent(user -> {
            if (!user.isVerified() && user.getVerificationExpiresAt() != null &&
                    Instant.now().isAfter(user.getVerificationExpiresAt())) {

                user.setVerificationCode(null);
                user.setVerificationExpiresAt(null);
                userRepository.save(user);
            }
        });
        pendingExpiry.remove(discordId);
    }

    private String makeCode() {
        String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String s = raw.substring(0, 12);
        return s.substring(0,4) + "-" + s.substring(4,8) + "-" + s.substring(8,12);
    }


    public boolean verifyOnLeetCode(String discordId) {

        return userRepository.findByDiscordId(discordId)
                .filter(u -> u.getVerificationCode() != null &&
                        u.getVerificationExpiresAt() != null &&
                        Instant.now().isBefore(u.getVerificationExpiresAt()))
                .map(user -> {

                    try {
                        // GraphQL request JSON
                        String body = """
                        {
                          "query": "query getUser($username: String!) { matchedUser(username: $username) { profile { aboutMe } } }",
                          "variables": { "username": "%s" }
                        }
                        """.formatted(user.getLeetcodeId());


                        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

                        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create("https://leetcode.com/graphql"))
                                .header("Content-Type", "application/json")
                                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                                .build();

                        java.net.http.HttpResponse<String> response =
                                client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                        String json = response.body();

                        // Debug log: see content fetched
                        System.out.println("GraphQL JSON = " + json);

                        // check if code appears in aboutMe text
                        if (json.contains(user.getVerificationCode())) {

                            user.setVerified(true);
                            user.setVerificationCode(null);
                            user.setVerificationExpiresAt(null);
                            userRepository.save(user);

                            ScheduledFuture<?> f = pendingExpiry.remove(discordId);
                            if (f != null) f.cancel(false);

                            return true;
                        }

                        return false;

                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }

                }).orElse(false);
    }
}
