package com.saumyabot.discord_bot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name="duels")
@Data
@NoArgsConstructor
public class Duel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String player1Id;
    private String player2Id;

    private String difficulty;
    private String questionSlug;

    private String winnerId;

    private Instant createdAt;
    private Instant startedAt;
    private Instant endedAt;

    private String status; // pending, active, finished , tc what
    private String channelId;
}
