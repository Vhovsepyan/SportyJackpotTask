package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Contributes a flat percentage of every stake, regardless of pool size:
 * {@code contribution = contributionPercentage * betAmount}.
 */
@Component
public class FixedPercentageContribution implements ContributionStrategy {

    @Override
    public ContributionType supportedType() {
        return ContributionType.FIXED;
    }

    @Override
    public BigDecimal calculate(BigDecimal betAmount, Jackpot jackpot) {
        return betAmount.multiply(jackpot.getContributionPercentage());
    }
}
