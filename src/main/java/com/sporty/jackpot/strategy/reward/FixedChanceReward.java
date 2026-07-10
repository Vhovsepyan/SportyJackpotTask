package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardType;
import org.springframework.stereotype.Component;

/**
 * A constant win chance, independent of pool size.
 */
@Component
public class FixedChanceReward implements RewardStrategy {

    @Override
    public RewardType supportedType() {
        return RewardType.FIXED;
    }

    @Override
    public double winChance(Jackpot jackpot) {
        return jackpot.getRewardChance().doubleValue();
    }
}
