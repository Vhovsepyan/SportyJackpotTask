package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.RewardType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RewardStrategyFactoryTest {

    private final RewardStrategyFactory factory = new RewardStrategyFactory(
            List.of(new FixedChanceReward(), new VariableChanceReward()));

    @Test
    void resolvesAStrategyForEveryEnumValue() {
        for (RewardType type : RewardType.values()) {
            assertThat(factory.strategyFor(type).supportedType()).isEqualTo(type);
        }
    }

    @Test
    void throwsWhenNoStrategyRegisteredForType() {
        RewardStrategyFactory empty = new RewardStrategyFactory(List.of());
        assertThatThrownBy(() -> empty.strategyFor(RewardType.VARIABLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No reward strategy registered");
    }

    @Test
    void rejectsDuplicateStrategiesForSameType() {
        assertThatThrownBy(() -> new RewardStrategyFactory(
                List.of(new FixedChanceReward(), new FixedChanceReward())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }
}
