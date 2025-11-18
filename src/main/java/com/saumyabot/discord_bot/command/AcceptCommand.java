package com.saumyabot.discord_bot.command;


import com.saumyabot.discord_bot.model.Duel;
import com.saumyabot.discord_bot.service.DuelService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

@Component
public class AcceptCommand {
    private final DuelService duelService;

    public AcceptCommand(DuelService duelService) {
        this.duelService = duelService;
    }

    public void execute(MessageCreateEvent event, String[] args){
        String userId= event.getMessage().getAuthor().get().getId().asString();

        var opt= duelService.acceptDuel(userId);
        if(opt.isPresent()){
            Duel d= opt.get();
            String msg= "Duel started between <@"+d.getPlayer1Id()+"> and <@"+d.getPlayer2Id()+">!\n"
                    + "Problem: https://leetcode.com/problems/" + d.getQuestionSlug() + "/\n"
                    + "First to AC wins. Duel times out after 90 minutes.";
            event.getMessage().getChannel().block().createMessage(msg).block();
        } else {
            event.getMessage().getChannel().block().createMessage("No pending duel to accept.").block();
        }
    }
}
