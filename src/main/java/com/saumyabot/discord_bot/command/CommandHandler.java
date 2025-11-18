package com.saumyabot.discord_bot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

@Component
public class CommandHandler {

    private final String PREFIX =">";
    private final RegisterCommand registerCommand;
    private final DoneCommand doneCommand;
    private final ProfileCommand profileCommand;
    private final StalkCommand stalkCommand;
    private final DuelCommand duelCommand;
    private final AcceptCommand acceptCommand;
    private  final DeclineCommand declineCommand;
    private final HelpCommand helpCommand;

    public CommandHandler(RegisterCommand registerCommand, DoneCommand doneCommand, ProfileCommand profileCommand, StalkCommand stalkCommand, DuelCommand duelCommand, AcceptCommand acceptCommand, DeclineCommand declineCommand, HelpCommand helpCommand) {
        this.registerCommand = registerCommand;
        this.doneCommand = doneCommand;
        this.profileCommand = profileCommand;
        this.stalkCommand = stalkCommand;
        this.duelCommand = duelCommand;
        this.acceptCommand = acceptCommand;
        this.declineCommand = declineCommand;
        this.helpCommand = helpCommand;
    }


    public void handle(MessageCreateEvent event){
        String msg= event.getMessage().getContent();

        if(!msg.startsWith(PREFIX)) return;

        String[] parts= msg.substring(PREFIX.length()).trim().split(" ");

        String command= parts[0];

        switch(command){
            case "ping" -> event.getMessage().getChannel()
                    .block().createMessage("Working!!").block();

            case "register" -> registerCommand.execute(event, parts);
            case "done" -> doneCommand.execute(event,parts);
            case "profile" -> profileCommand.execute(event, parts);
            case "stalk" -> stalkCommand.execute(event, parts);
            case "duel" -> duelCommand.execute(event, parts);
            case "accept"-> acceptCommand.execute(event, parts);
            case "decline"-> declineCommand.execute(event, parts);
            case "help" -> helpCommand.execute(event,parts);
            default -> event.getMessage().getChannel().block()
                    .createMessage("Unknown command").block();
        }
    }
}
