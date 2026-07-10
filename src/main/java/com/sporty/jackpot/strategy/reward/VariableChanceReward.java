package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * A win chance that starts low and grows linearly with the pool between
 * {@code initialPool} and {@code rewardPoolLimit}, reaching 100% once the pool
 * hits the limit:
 *
 * <pre>
 *   pool &lt;= initialPool            -> startChance
 *   initialPool &lt; pool &lt; limit     -> startChance + (1 - startChance) * (pool - initialPool) / (limit - initialPool)
 *   pool &gt;= limit                  -> 1.0
 * </pre>
 *
 * This guarantees the jackpot is paid out by the time the pool reaches the cap.
 */
@Component
public class VariableChanceReward implements RewardStrategy {

    @Override
    public RewardType supportedType() {
        return RewardType.VARIABLE;
    }

    @Override
    public double winChance(Jackpot jackpot) {
        BigDecimal pool = jackpot.getCurrentPool();
        BigDecimal initial = jackpot.getInitialPool();
        BigDecimal limit = jackpot.getRewardPoolLimit();

        if (pool.compareTo(limit) >= 0) {
            return 1.0;
        }
        if (pool.compareTo(initial) <= 0) {
            return jackpot.getRewardChance().doubleValue();
        }

        double startChance = jackpot.getRewardChance().doubleValue();
        double progress = pool.subtract(initial).doubleValue()
                / limit.subtract(initial).doubleValue();
        return startChance + (1.0 - startChance) * progress;
    }
}
