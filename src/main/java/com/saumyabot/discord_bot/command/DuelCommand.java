package com.saumyabot.discord_bot.command;

import com.saumyabot.discord_bot.model.Duel;
import com.saumyabot.discord_bot.model.User;
import com.saumyabot.discord_bot.service.DuelService;
import com.saumyabot.discord_bot.service.UserService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

@Component
public class DuelCommand {

    private final UserService userService;
    private final DuelService duelService;

    public DuelCommand(UserService userService, DuelService duelService) {
        this.userService = userService;
        this.duelService = duelService;
    }

    private int difficultyToInt(String diff) {
        return switch (diff) {
            case "easy" -> 1;
            case "medium" -> 2;
            case "hard" -> 3;
            default -> 2;
        };
    }

    public void execute(MessageCreateEvent event, String[] args) {

        if (event.getMessage().getUserMentions().isEmpty() || args.length < 2) {
            event.getMessage().getChannel().block()
                    .createMessage("Usage: `>duel @user <easy|medium|hard>`")
                    .block();
            return;
        }

        String challengerId = event.getMessage().getAuthor().get().getId().asString();
        String opponentId = event.getMessage().getUserMentions().get(0).getId().asString();
        String diff = args[args.length - 1].toLowerCase();

        if (challengerId.equals(opponentId)) {
            event.getMessage().getChannel().block()
                    .createMessage("You cannot duel yourself.")
                    .block();
            return;
        }

        if (!diff.equals("easy") && !diff.equals("medium") && !diff.equals("hard")) {
            event.getMessage().getChannel().block()
                    .createMessage("Invalid difficulty. Use: easy / medium / hard")
                    .block();
            return;
        }

        User challenger = userService.findByDiscordId(challengerId).orElse(null);
        User opponent = userService.findByDiscordId(opponentId).orElse(null);

        if (challenger == null || opponent == null || !challenger.isVerified() || !opponent.isVerified()) {
            event.getMessage().getChannel().block()
                    .createMessage("Both users must be verified.")
                    .block();
            return;
        }

        if (challenger.getCurrentDuelId() != null) {
            event.getMessage().getChannel().block()
                    .createMessage("You are already in a duel.")
                    .block();
            return;
        }

        if (opponent.getCurrentDuelId() != null) {
            event.getMessage().getChannel().block()
                    .createMessage("That user is already in a duel.")
                    .block();
            return;
        }

        String channelId = event.getMessage().getChannelId().asString();
        int diffInt = difficultyToInt(diff);

        Duel duel = duelService.createDuel(challengerId, opponentId, diffInt, channelId);

        event.getMessage().getChannel().block().createMessage(
                "<@" + opponentId + "> you have been challenged to a " + diff +
                        " duel by <@" + challengerId + ">! Type `>accept` or `>decline` within 2 minutes."
        ).block();
    }
}
