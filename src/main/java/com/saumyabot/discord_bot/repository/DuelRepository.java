package com.saumyabot.discord_bot.repository;

import com.saumyabot.discord_bot.model.Duel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DuelRepository extends JpaRepository<Duel, Long> {
    Optional<Duel> findById(Long id);
}
