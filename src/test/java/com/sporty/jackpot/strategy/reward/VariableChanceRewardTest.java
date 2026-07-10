package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class VariableChanceRewardTest {

    private final VariableChanceReward strategy = new VariableChanceReward();

    /** start 1%, initial pool 1000, 100% at pool 10000. */
    private Jackpot variableJackpot(String currentPool) {
        return Jackpot.builder()
                .id("j")
                .initialPool(new BigDecimal("1000"))
                .currentPool(new BigDecimal(currentPool))
                .rewardType(RewardType.VARIABLE)
                .rewardChance(new BigDecimal("0.01"))
                .rewardPoolLimit(new BigDecimal("10000"))
                .build();
    }

    @Test
    void supportsVariableType() {
        assertThat(strategy.supportedType()).isEqualTo(RewardType.VARIABLE);
    }

    @Test
    void returnsStartChanceAtInitialPool() {
        assertThat(strategy.winChance(variableJackpot("1000"))).isEqualTo(0.01);
    }

    @Test
    void interpolatesLinearlyAtMidpoint() {
        // midpoint pool 5500 -> progress 0.5 -> 0.01 + 0.99*0.5 = 0.505
        assertThat(strategy.winChance(variableJackpot("5500"))).isCloseTo(0.505, within(1e-9));
    }

    @Test
    void reachesFullChanceAtPoolLimit() {
        assertThat(strategy.winChance(variableJackpot("10000"))).isEqualTo(1.0);
    }

    @Test
    void staysAtFullChanceAbovePoolLimit() {
        assertThat(strategy.winChance(variableJackpot("25000"))).isEqualTo(1.0);
    }
}
