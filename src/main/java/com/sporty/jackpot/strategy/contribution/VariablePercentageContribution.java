package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Contributes a percentage that starts high and decays linearly as the pool
 * grows above its initial value, never dropping below a configured floor:
 *
 * <pre>
 *   pct = max(floorPct, startPct - decayRate * (currentPool - initialPool))
 *   contribution = pct * betAmount
 * </pre>
 *
 * This rewards early bettors (larger contributions while the pool is small) and
 * tapers off as the pool fills up.
 */
@Component
public class VariablePercentageContribution implements ContributionStrategy {

    @Override
    public ContributionType supportedType() {
        return ContributionType.VARIABLE;
    }

    @Override
    public BigDecimal calculate(BigDecimal betAmount, Jackpot jackpot) {
        BigDecimal poolGrowth = jackpot.getCurrentPool().subtract(jackpot.getInitialPool());
        BigDecimal decayed = jackpot.getContributionPercentage()
                .subtract(jackpot.getContributionDecayRate().multiply(poolGrowth));
        BigDecimal effectivePercentage = decayed.max(jackpot.getContributionFloorPercentage());
        return betAmount.multiply(effectivePercentage);
    }
}
