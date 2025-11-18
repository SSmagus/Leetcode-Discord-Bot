package com.saumyabot.discord_bot.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder
                .create(System.getenv("DISCORD_TOKEN"))
                .build()
                .login()
                .block();
    }
}
