package com.saumyabot.discord_bot.listener;

import com.saumyabot.discord_bot.command.CommandHandler;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    private final GatewayDiscordClient client;
    private final CommandHandler commandHandler;

    public  MessageListener(GatewayDiscordClient client, CommandHandler commandHandler){
        this.client=client;
        this.commandHandler= commandHandler;
    }

    @PostConstruct
    public void register(){
        client.on(MessageCreateEvent.class)
                .subscribe(event-> commandHandler.handle(event));
    }
}
