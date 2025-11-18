package com.saumyabot.discord_bot.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.saumyabot.discord_bot.model.User;
import com.saumyabot.discord_bot.service.LeetCodeService;
import com.saumyabot.discord_bot.service.UserService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;
import discord4j.core.spec.EmbedCreateSpec;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class StalkCommand {

    private final UserService userService;
    private final LeetCodeService leetCodeService;

    public StalkCommand(UserService userService, LeetCodeService leetCodeService) {
        this.userService = userService;
        this.leetCodeService = leetCodeService;
    }

    public void execute(MessageCreateEvent event, String[] args) {

        if (event.getMessage().getUserMentions().isEmpty()) {
            event.getMessage().getChannel().block()
                    .createMessage("Usage: `>stalk @user`")
                    .block();
            return;
        }

        String mentionId = event.getMessage()
                .getUserMentions()
                .get(0)
                .getId()
                .asString();

        User user = userService.findByDiscordId(mentionId).orElse(null);

        if (user == null || !user.isVerified()) {
            event.getMessage().getChannel().block()
                    .createMessage("❌ That user is not verified.")
                    .block();
            return;
        }

        JsonNode data = leetCodeService.fetchProfile(user.getLeetcodeId());

        if (data == null) {
            event.getMessage().getChannel().block()
                    .createMessage("⚠️ Could not fetch their LeetCode data.")
                    .block();
            return;
        }

        JsonNode submissions = data.get("recentSubmissionList");

        if (submissions == null || !submissions.isArray()) {
            event.getMessage().getChannel().block()
                    .createMessage("No recent submissions found.")
                    .block();
            return;
        }

        StringBuilder sb = new StringBuilder();
        int count = 1;

        for (JsonNode s : submissions) {

            if (!"Accepted".equals(s.get("statusDisplay").asText()))
                continue;

            String title = s.get("title").asText();
            String slug = s.get("titleSlug").asText();
            long timestamp = s.get("timestamp").asLong();

            String link = "https://leetcode.com/problems/" + slug + "/";
            String timeAgo = timeAgo(timestamp);

            sb.append("**[").append(title).append("](").append(link).append(")**")
                    .append(" — *").append(timeAgo).append("*\n");

            if (count++ == 10) break;
        }

        if (count == 1)
            sb.append("No recent accepted submissions.");

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Recent Solves — " + user.getLeetcodeId())
                .description(sb.toString())
                .color(Color.of(0x7A91FF))
                .build();

        event.getMessage().getChannel().block()
                .createMessage(embed).block();
    }

    private String timeAgo(long unix) {
        Instant then = Instant.ofEpochSecond(unix);
        Duration diff = Duration.between(then, Instant.now());

        if (diff.toMinutes() < 60)
            return diff.toMinutes() + " minutes ago";

        if (diff.toHours() < 24)
            return diff.toHours() + " hours ago";

        return diff.toDays() + " days ago";
    }
}
