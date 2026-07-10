package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FixedChanceRewardTest {

    private final FixedChanceReward strategy = new FixedChanceReward();

    @Test
    void supportsFixedType() {
        assertThat(strategy.supportedType()).isEqualTo(RewardType.FIXED);
    }

    @Test
    void returnsConstantChanceRegardlessOfPool() {
        Jackpot jackpot = Jackpot.builder()
                .id("j")
                .initialPool(new BigDecimal("1000"))
                .currentPool(new BigDecimal("1000"))
                .rewardType(RewardType.FIXED)
                .rewardChance(new BigDecimal("0.10"))
                .build();

        assertThat(strategy.winChance(jackpot)).isEqualTo(0.10);

        jackpot.setCurrentPool(new BigDecimal("500000"));
        assertThat(strategy.winChance(jackpot)).isEqualTo(0.10);
    }
}
