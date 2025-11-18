package com.saumyabot.discord_bot.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.saumyabot.discord_bot.model.User;
import com.saumyabot.discord_bot.service.LeetCodeService;
import com.saumyabot.discord_bot.service.UserService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;

@Component
public class ProfileCommand {

    private final UserService userService;
    private final LeetCodeService leetCodeService;

    public ProfileCommand(UserService userService, LeetCodeService leetCodeService) {
        this.userService = userService;
        this.leetCodeService = leetCodeService;
    }

    public void execute(MessageCreateEvent event, String[] args) {

        String discordId = event.getMessage().getAuthor().get().getId().asString();
        User user = userService.findByDiscordId(discordId).orElse(null);

        if (user == null || !user.isVerified()) {
            event.getMessage().getChannel().block()
                    .createMessage("❌ You are not verified yet. Use `>register <leetcodeId>` first.")
                    .block();
            return;
        }

        JsonNode data = leetCodeService.fetchProfile(user.getLeetcodeId());
        if (data == null || data.isNull()) {
            event.getMessage().getChannel().block()
                    .createMessage("⚠️ Could not fetch your LeetCode profile.")
                    .block();
            return;
        }

        JsonNode profile = data.get("matchedUser");
        JsonNode contest = data.get("userContestRanking");

        if (profile == null || profile.isNull()) {
            event.getMessage().getChannel().block()
                    .createMessage("⚠️ Could not find profile details for that LeetCode username.")
                    .block();
            return;
        }

        String username = profile.path("username").asText(user.getLeetcodeId());


        JsonNode ac = profile.path("submitStatsGlobal").path("acSubmissionNum");

        int total = safe(ac, 0);   // ALL
        int easy  = safe(ac, 1);   // EASY
        int medium= safe(ac, 2);   // MEDIUM
        int hard  = safe(ac, 3);   // HARD


        int rating = contest != null && contest.hasNonNull("rating")
                ? contest.get("rating").asInt(-1) : -1;

        int globalRank = contest != null && contest.hasNonNull("globalRanking")
                ? contest.get("globalRanking").asInt(-1) : -1;

        double percentile = contest != null && contest.hasNonNull("topPercentage")
                ? contest.get("topPercentage").asDouble(-1) : -1;

        int ranking = profile.path("profile").path("ranking").asInt(-1);
        int streak = profile.path("userCalendar").path("streak").asInt(0);
        String realName = profile.path("profile").path("realName").asText("");


        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("📘 LeetCode Profile — " + username)
                .color(Color.of(0xF5A623))
                .description(realName.isEmpty() ? "" : "*" + realName + "*")

                .addField("🏆 Contest Rating",
                        "Rating: **" + (rating == -1 ? "N/A" : rating) + "**\n"
                                + "Global Rank: **" + (globalRank == -1 ? "N/A" : globalRank) + "**\n"
                                + "Top %: **" + (percentile == -1 ? "N/A" : String.format("%.2f%%", percentile)) + "**",
                        false)

                .addField("📊 Profile Stats",
                        "Ranking: **" + (ranking == -1 ? "N/A" : ranking) + "**\n"
                                + "Streak: **" + streak + "** days",
                        false)

                .addField("🧩 Solved Problems",
                        "Easy: **" + easy + "**\n"
                                + "Medium: **" + medium + "**\n"
                                + "Hard: **" + hard + "**\n"
                                + "Total: **" + total + "**",
                        false)

                .build();

        event.getMessage().getChannel().block()
                .createMessage(embed)
                .block();
    }

    private int safe(JsonNode arr, int idx) {
        try {
            return arr.get(idx).path("count").asInt(0);
        } catch (Exception e) {
            return 0;
        }
    }
}
