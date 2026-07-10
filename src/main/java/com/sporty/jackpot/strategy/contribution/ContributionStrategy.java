package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;

import java.math.BigDecimal;

/**
 * Computes how much of a bet's stake is contributed to a jackpot pool.
 *
 * <p>Each implementation declares the {@link ContributionType} it handles so the
 * {@link ContributionStrategyFactory} can wire it up automatically: adding a new
 * contribution behaviour is just a new implementation plus a new enum value.</p>
 */
public interface ContributionStrategy {

    /**
     * @return the enum value this strategy handles.
     */
    ContributionType supportedType();

    /**
     * @param betAmount the stake placed on the bet
     * @param jackpot   the jackpot being contributed to (source of the tunable parameters)
     * @return the amount to add to the jackpot's current pool
     */
    BigDecimal calculate(BigDecimal betAmount, Jackpot jackpot);
}
