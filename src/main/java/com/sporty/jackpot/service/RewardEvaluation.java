package com.sporty.jackpot.service;

import java.math.BigDecimal;

/**
 * Outcome of evaluating a bet for a jackpot reward.
 *
 * @param betId        the evaluated bet
 * @param won          whether the bet won the jackpot
 * @param rewardAmount the amount awarded (only set when {@code won} is true)
 */
public record RewardEvaluation(String betId, boolean won, BigDecimal rewardAmount) {

    public static RewardEvaluation win(String betId, BigDecimal rewardAmount) {
        return new RewardEvaluation(betId, true, rewardAmount);
    }

    public static RewardEvaluation loss(String betId) {
        return new RewardEvaluation(betId, false, null);
    }
}
