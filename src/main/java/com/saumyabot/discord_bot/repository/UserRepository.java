package com.saumyabot.discord_bot.repository;

import com.saumyabot.discord_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByDiscordId(String discordId);
    Optional<User> findByLeetcodeId(String leetcodeId);
}
