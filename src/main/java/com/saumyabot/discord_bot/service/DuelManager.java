package com.saumyabot.discord_bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.saumyabot.discord_bot.model.Duel;
import com.saumyabot.discord_bot.model.User;
import com.saumyabot.discord_bot.repository.DuelRepository;
import com.saumyabot.discord_bot.repository.UserRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class DuelManager {

    private final Map<Long, ScheduledFuture<?>> pendingTimers = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> pollers = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> duelTimeouts = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final DuelService duelService;
    private final LeetCodeService leetCodeService;
    private final GatewayDiscordClient gateway;
    private final DuelRepository duelRepository;
    private final UserRepository userRepository;

    public DuelManager(@Lazy DuelService duelService,
                       LeetCodeService leetCodeService,
                       GatewayDiscordClient gateway,
                       DuelRepository duelRepository,
                       UserRepository userRepository) {

        this.duelService = duelService;
        this.leetCodeService = leetCodeService;
        this.gateway = gateway;
        this.duelRepository = duelRepository;
        this.userRepository = userRepository;
    }

    public LeetCodeService getLeetCodeService() {
        return leetCodeService;
    }

    public void startPendingTimer(Long duelId) {
        ScheduledFuture<?> f = scheduler.schedule(() -> {
            duelRepository.findById(duelId).ifPresent(d -> {
                if ("PENDING".equals(d.getStatus())) {
                    d.setStatus("CANCELLED");
                    d.setEndedAt(Instant.now());
                    duelRepository.save(d);
                    sendToChannel(d.getChannelId(), "Duel request timed out.");
                }
            });
            pendingTimers.remove(duelId);
        }, 2, TimeUnit.MINUTES);

        pendingTimers.put(duelId, f);
    }

    public void stopPendingTimer(Long duelId) {
        ScheduledFuture<?> f = pendingTimers.remove(duelId);
        if (f != null) f.cancel(false);
    }

    public void startActiveTimers(Duel duel) {
        Long id = duel.getId();
        stopPendingTimer(id);

        ScheduledFuture<?> poll = scheduler.scheduleAtFixedRate(() -> {
            try {
                Duel d = duelRepository.findById(id).orElse(null);
                if (d == null || !"ACTIVE".equals(d.getStatus())) {
                    stopPoller(id);
                    return;
                }

                String slug = d.getQuestionSlug();
                checkPlayerForWin(d, d.getPlayer1Id(), slug);
                checkPlayerForWin(d, d.getPlayer2Id(), slug);

            } catch (Exception ignored) {}
        }, 0, 10, TimeUnit.SECONDS);

        pollers.put(id, poll);

        ScheduledFuture<?> timeout = scheduler.schedule(() -> {
            duelRepository.findById(id).ifPresent(d -> {
                if (!"FINISHED".equals(d.getStatus())) {
                    d.setStatus("TIMEOUT");
                    d.setEndedAt(Instant.now());
                    duelRepository.save(d);
                    sendToChannel(d.getChannelId(), "Duel timed out after 90 minutes.");
                    stopPoller(id);
                }
            });
            duelTimeouts.remove(id);
        }, 90, TimeUnit.MINUTES);

        duelTimeouts.put(id, timeout);
    }

    private void stopPoller(Long duelId) {
        ScheduledFuture<?> p = pollers.remove(duelId);
        if (p != null) p.cancel(false);

        ScheduledFuture<?> t = duelTimeouts.remove(duelId);
        if (t != null) t.cancel(false);
    }

    private String getLeetCodeUsername(String discordId) {
        return userRepository.findByDiscordId(discordId)
                .map(User::getLeetcodeId)
                .orElse(null);
    }

    private void checkPlayerForWin(Duel d, String playerId, String slug) {
        if (slug == null || slug.isEmpty()) return;

        JsonNode arr = leetCodeService.fetchRecentSubmissionList(getLeetCodeUsername(playerId));
        if (arr == null || !arr.isArray()) return;

        for (JsonNode s : arr) {
            String status = s.path("statusDisplay").asText().toLowerCase();
            if (!status.contains("accepted")) continue;

            String titleSlug = s.path("titleSlug").asText();
            long ts = s.path("timestamp").asLong();
            long started = d.getStartedAt().getEpochSecond();

            if (slug.equals(titleSlug) && ts >= started - 5) {
                finishDuelWithWinner(d, playerId);
                return;
            }
        }
    }

    private void finishDuelWithWinner(Duel d, String winnerId) {

        stopPoller(d.getId());

        String loserId = d.getPlayer1Id().equals(winnerId)
                ? d.getPlayer2Id()
                : d.getPlayer1Id();

        int ratingChange = duelService.finishDuel(d.getId(), winnerId, loserId);

        sendToChannel(
                d.getChannelId(),
                "<@" + winnerId + "> wins the duel! +" + ratingChange + " rating.\n" +
                        "https://leetcode.com/problems/" + d.getQuestionSlug() + "/"
        );
    }

    private void sendToChannel(String channelId, String message) {
        try {
            gateway.getChannelById(Snowflake.of(channelId))
                    .cast(MessageChannel.class)
                    .block()
                    .createMessage(message)
                    .block();
        } catch (Exception ignored) {}
    }
}
