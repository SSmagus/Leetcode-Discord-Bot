package com.saumyabot.discord_bot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String discordId;

    private String leetcodeId;

    private boolean verified = false;
    @Column(length = 100)
    private String verificationCode;
    private Instant verificationExpiresAt;
    private int duelRating =1000;
    private int duelWins = 0;
    private int duelLosses=0;
    private Long currentDuelId;

}
