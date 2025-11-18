package com.saumyabot.discord_bot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand {

    public void execute(MessageCreateEvent event, String[] args) {

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("📜 LeetHost Help Menu")
                .description("All available commands")
                .addField("👤 **Profile Commands**",
                        ">register `<leetcodeId>` — Link & verify your LC account\n" +
                                ">profile — View your verified LeetCode stats\n" +
                                ">stalk `<leetcodeId or @user>` — Recently solved problems",
                        false)
                .addField("⚔️ **Duel Commands**",
                        ">duel `@user <easy|medium|hard>` — Challenge someone\n" +
                                ">accept — Accept a pending duel\n" +
                                ">decline — Decline a duel\n" +
                                ">forfeit — (Coming soon) Quit an active duel",
                        false)
                .addField("ℹ️ **General**",
                        ">help — Show this help menu",
                        false)
                .color(Color.of(0xFFD700))
                .build();

        event.getMessage().getChannel().block()
                .createMessage(embed)
                .block();
    }
}
