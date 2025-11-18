package com.saumyabot.discord_bot.service;

import com.saumyabot.discord_bot.model.Duel;
import com.saumyabot.discord_bot.model.Problem;
import com.saumyabot.discord_bot.model.User;
import com.saumyabot.discord_bot.repository.DuelRepository;
import com.saumyabot.discord_bot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class DuelService {

    private final UserRepository userRepository;
    private final DuelRepository duelRepository;
    private final DuelManager duelManager;

    @Autowired
    private ProblemService problemService; // new

    public DuelService(UserRepository userRepository, DuelRepository duelRepository, DuelManager duelManager) {
        this.userRepository = userRepository;
        this.duelRepository = duelRepository;
        this.duelManager = duelManager;
    }


    private int mapDifficultyToInt(int difficultyInput) {
        // YOU pass difficulty as int to createDuel(), so just return it
        // 1 = easy, 2 = medium, 3 = hard
        if (difficultyInput < 1 || difficultyInput > 3) return 2;
        return difficultyInput;
    }


    public Duel createDuel(String p1, String p2, int difficulty, String channelId) {
        Duel duel = new Duel();

        duel.setPlayer1Id(p1);
        duel.setPlayer2Id(p2);
        duel.setChannelId(channelId);
        duel.setStatus("PENDING");
        duel.setCreatedAt(Instant.now());

        int diffInt = mapDifficultyToInt(difficulty);   // 1,2,3
        Problem prob = problemService.getRandomProblem(diffInt);

        duel.setQuestionSlug(prob.getSlug());
        duel.setDifficulty(String.valueOf(difficulty));  // store as string (your DB model)

        duelRepository.save(duel);

        return duel;
    }


    public Optional<Duel> acceptDuel(String acceptorDiscordId){
        Optional<Duel> opt = duelRepository.findAll().stream()
                .filter(d -> "PENDING".equals(d.getStatus()) &&
                        acceptorDiscordId.equals(d.getPlayer2Id()))
                .findFirst();

        if(opt.isPresent()) {
            Duel d = opt.get();
            d.setStatus("ACTIVE");
            d.setStartedAt(Instant.now());

            duelRepository.save(d);

            userRepository.findByDiscordId(d.getPlayer1Id())
                    .ifPresent(u -> { u.setCurrentDuelId(d.getId()); userRepository.save(u); });

            userRepository.findByDiscordId(d.getPlayer2Id())
                    .ifPresent(u -> { u.setCurrentDuelId(d.getId()); userRepository.save(u); });

            duelManager.startActiveTimers(d);
            return Optional.of(d);
        }

        return Optional.empty();
    }

    public boolean declineDuel(String declinerDiscordId){
        Optional<Duel> opt = duelRepository.findAll().stream()
                .filter(d -> "PENDING".equals(d.getStatus()) &&
                        declinerDiscordId.equals(d.getPlayer2Id()))
                .findFirst();

        if(opt.isPresent()){
            Duel d = opt.get();
            d.setStatus("CANCELLED");
            d.setEndedAt(Instant.now());
            duelRepository.save(d);
            duelManager.stopPendingTimer(d.getId());
            return true;
        }

        return false;
    }


    public int finishDuel(Long duelId, String winnerDiscordId, String loserDiscordId) {
        Duel d = duelRepository.findById(duelId).orElseThrow();
        d.setWinnerId(winnerDiscordId);
        d.setStatus("FINISHED");
        d.setEndedAt(Instant.now());
        duelRepository.save(d);

        User winner = userRepository.findByDiscordId(winnerDiscordId).orElseThrow();
        User loser = userRepository.findByDiscordId(loserDiscordId).orElseThrow();

        int ra = winner.getDuelRating();
        int rb = loser.getDuelRating();

        double expectedA = 1.0 / (1.0 + Math.pow(10.0, (rb - ra) / 400.0));
        int K = 30;

        int newA = (int) Math.round(ra + K * (1 - expectedA));
        int newB = (int) Math.round(rb + K * (0 - (1 - expectedA)));

        int ratingChange = newA - ra;

        winner.setDuelWins(winner.getDuelWins() + 1);
        winner.setDuelRating(newA);
        winner.setCurrentDuelId(null);

        loser.setDuelLosses(loser.getDuelLosses() + 1);
        loser.setDuelRating(newB);
        loser.setCurrentDuelId(null);

        userRepository.save(winner);
        userRepository.save(loser);

        return ratingChange;
    }
}
