package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FixedPercentageContributionTest {

    private final FixedPercentageContribution strategy = new FixedPercentageContribution();

    private Jackpot fixedJackpot(String percentage) {
        return Jackpot.builder()
                .id("j")
                .initialPool(new BigDecimal("1000"))
                .currentPool(new BigDecimal("1000"))
                .contributionType(ContributionType.FIXED)
                .contributionPercentage(new BigDecimal(percentage))
                .build();
    }

    @Test
    void supportsFixedType() {
        assertThat(strategy.supportedType()).isEqualTo(ContributionType.FIXED);
    }

    @Test
    void contributesFlatPercentageOfStake() {
        BigDecimal contribution = strategy.calculate(new BigDecimal("100"), fixedJackpot("0.05"));
        assertThat(contribution).isEqualByComparingTo("5");
    }

    @Test
    void contributionIsIndependentOfPoolSize() {
        Jackpot jackpot = fixedJackpot("0.05");
        jackpot.setCurrentPool(new BigDecimal("999999"));
        assertThat(strategy.calculate(new BigDecimal("200"), jackpot)).isEqualByComparingTo("10");
    }
}
