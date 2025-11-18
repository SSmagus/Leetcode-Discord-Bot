package com.saumyabot.discord_bot.listener;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ReadyListener {

    private final GatewayDiscordClient client;

    public ReadyListener(GatewayDiscordClient client) {
        this.client = client;
    }

    @PostConstruct
    public void register() {
        client.on(ReadyEvent.class)
                .subscribe(event ->
                        System.out.println("Bot connected as: " + event.getSelf().getUsername())
                );
    }
}
