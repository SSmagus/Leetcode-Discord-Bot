package com.saumyabot.discord_bot.command;

import com.saumyabot.discord_bot.service.DuelService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

@Component
public class DeclineCommand {
    private final DuelService duelService;

    public DeclineCommand(DuelService duelService) {
        this.duelService = duelService;
    }

    public void execute(MessageCreateEvent event, String[] args){
        String userId= event.getMessage().getAuthor().get().getId().asString();
        boolean ok= duelService.declineDuel(userId);

        if(ok){
            event.getMessage().getChannel().block().createMessage("Duel declined.").block();
        }
        else{
            event.getMessage().getChannel().block().createMessage("No pending duel to decline.").block();
        }
    }
}
