package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContributionStrategyFactoryTest {

    private final ContributionStrategyFactory factory = new ContributionStrategyFactory(
            List.of(new FixedPercentageContribution(), new VariablePercentageContribution()));

    @Test
    void resolvesAStrategyForEveryEnumValue() {
        for (ContributionType type : ContributionType.values()) {
            assertThat(factory.strategyFor(type).supportedType()).isEqualTo(type);
        }
    }

    @Test
    void throwsWhenNoStrategyRegisteredForType() {
        ContributionStrategyFactory empty = new ContributionStrategyFactory(List.of());
        assertThatThrownBy(() -> empty.strategyFor(ContributionType.FIXED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No contribution strategy registered");
    }

    @Test
    void rejectsDuplicateStrategiesForSameType() {
        assertThatThrownBy(() -> new ContributionStrategyFactory(
                List.of(new FixedPercentageContribution(), new FixedPercentageContribution())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }
}
