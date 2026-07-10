package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardType;

/**
 * Computes the probability that a bet wins the jackpot.
 *
 * <p>Each implementation declares the {@link RewardType} it handles so the
 * {@link RewardStrategyFactory} can wire it up automatically.</p>
 */
public interface RewardStrategy {

    /**
     * @return the enum value this strategy handles.
     */
    RewardType supportedType();

    /**
     * @param jackpot the jackpot being evaluated (source of the tunable parameters)
     * @return the win chance as a probability in the range {@code [0.0, 1.0]}
     */
    double winChance(Jackpot jackpot);
}
