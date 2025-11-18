package com.saumyabot.discord_bot.command;

import com.saumyabot.discord_bot.service.VerificationService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

@Component
public class DoneCommand {

    private final VerificationService verificationService;

    public DoneCommand(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    public void execute(MessageCreateEvent event, String[] args) {

        String discordId = event.getMessage().getAuthor().get().getId().asString();

        boolean success = verificationService.verifyOnLeetCode(discordId);

        if (success) {
            event.getMessage().getChannel().block()
                    .createMessage("✅ **Verification successful!** Your account is now linked.").block();
        } else {
            event.getMessage().getChannel().block()
                    .createMessage("❌ **Verification failed.** Make sure the code is in your LeetCode About Me.").block();
        }
    }
}
