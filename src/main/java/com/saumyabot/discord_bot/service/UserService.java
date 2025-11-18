package com.saumyabot.discord_bot.service;

import com.saumyabot.discord_bot.model.User;
import com.saumyabot.discord_bot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo){
        this.repo= repo;
    }

    public Optional<User> findByDiscordId(String discordId){
        return repo.findByDiscordId(discordId);
    }
}
