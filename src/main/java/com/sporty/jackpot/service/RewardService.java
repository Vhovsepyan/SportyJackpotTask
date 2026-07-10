package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContribution;
import com.sporty.jackpot.domain.JackpotReward;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import com.sporty.jackpot.strategy.reward.RewardStrategy;
import com.sporty.jackpot.strategy.reward.RewardStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Evaluates a bet for a jackpot reward.
 *
 * <p>Evaluation is idempotent: once a bet has won, re-evaluating returns the
 * same result and never pays out twice. Winning is decided by the jackpot's
 * reward strategy plus a random roll; on a win the pool is awarded in full and
 * reset to its initial value, all within a single transaction.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardService {

    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository contributionRepository;
    private final JackpotRewardRepository rewardRepository;
    private final RewardStrategyFactory rewardStrategyFactory;
    private final RandomProvider randomProvider;

    @Transactional
    public RewardEvaluation evaluate(String betId) {
        // Idempotency: a bet that already won returns its existing reward.
        Optional<JackpotReward> existingReward = rewardRepository.findByBetId(betId);
        if (existingReward.isPresent()) {
            JackpotReward reward = existingReward.get();
            log.info("Bet '{}' already won {} — returning existing reward", betId, reward.getJackpotRewardAmount());
            return RewardEvaluation.win(betId, reward.getJackpotRewardAmount());
        }

        JackpotContribution contribution = contributionRepository.findByBetId(betId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No contribution found for betId '" + betId + "' — the bet never contributed"));

        Jackpot jackpot = jackpotRepository.findById(contribution.getJackpotId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Jackpot '" + contribution.getJackpotId() + "' not found"));

        RewardStrategy strategy = rewardStrategyFactory.strategyFor(jackpot.getRewardType());
        double winChance = strategy.winChance(jackpot);
        double roll = randomProvider.nextDouble();

        if (roll >= winChance) {
            log.info("Bet '{}' did not win jackpot '{}' (roll {} >= chance {})",
                    betId, jackpot.getId(), roll, winChance);
            return RewardEvaluation.loss(betId);
        }

        BigDecimal rewardAmount = jackpot.getCurrentPool();
        JackpotReward reward = JackpotReward.builder()
                .betId(betId)
                .userId(contribution.getUserId())
                .jackpotId(jackpot.getId())
                .jackpotRewardAmount(rewardAmount)
                .createdAt(Instant.now())
                .build();
        rewardRepository.save(reward);

        // Award the whole pool and reset to the initial seed value.
        jackpot.setCurrentPool(jackpot.getInitialPool());
        jackpotRepository.save(jackpot);

        log.info("Bet '{}' WON jackpot '{}' for {} (roll {} < chance {}); pool reset to {}",
                betId, jackpot.getId(), rewardAmount, roll, winChance, jackpot.getInitialPool());
        return RewardEvaluation.win(betId, rewardAmount);
    }
}
