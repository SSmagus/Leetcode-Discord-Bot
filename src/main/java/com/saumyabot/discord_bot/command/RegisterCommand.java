package com.saumyabot.discord_bot.command;

import com.saumyabot.discord_bot.service.VerificationService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

@Component
public class RegisterCommand {
    private  final VerificationService verificationService;

    public RegisterCommand(VerificationService verificationService){
        this.verificationService= verificationService;
    }

    public void execute(MessageCreateEvent event, String[] args){

        if(args.length<2){
            event.getMessage().getChannel().block()
                    .createMessage("Usage: `>register <leetcode_id>`").block();
            return;
        }

        String leetId= args[1];
        String discordId= event.getMessage().getAuthor().get().getId().asString();

        String code= verificationService.generateCodeAndSave(discordId, leetId);

        String message = """
                **🔐 Verification Required**

                Add this code to your LeetCode **About Me**:

                **%s**

                Then type: `>done`
                This code expires in **5 minutes**.
                """.formatted(code);

        event.getMessage().getChannel().block().createMessage(message).block();
    }
}
