package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionType;
import com.sporty.jackpot.domain.Jackpot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class VariablePercentageContributionTest {

    private final VariablePercentageContribution strategy = new VariablePercentageContribution();

    /** start 10%, decays 1% per 1000 of pool growth, floor 1%. */
    private Jackpot variableJackpot(String currentPool) {
        return Jackpot.builder()
                .id("j")
                .initialPool(new BigDecimal("1000"))
                .currentPool(new BigDecimal(currentPool))
                .contributionType(ContributionType.VARIABLE)
                .contributionPercentage(new BigDecimal("0.10"))
                .contributionDecayRate(new BigDecimal("0.00001"))
                .contributionFloorPercentage(new BigDecimal("0.01"))
                .build();
    }

    @Test
    void supportsVariableType() {
        assertThat(strategy.supportedType()).isEqualTo(ContributionType.VARIABLE);
    }

    @Test
    void usesStartPercentageWhenPoolIsAtInitial() {
        // growth = 0 -> pct = 10% -> 10% of 100 = 10
        assertThat(strategy.calculate(new BigDecimal("100"), variableJackpot("1000")))
                .isEqualByComparingTo("10");
    }

    @Test
    void percentageDecaysAsPoolGrows() {
        // growth = 1000 -> decay = 0.01 -> pct = 0.09 -> 0.09 * 100 = 9
        assertThat(strategy.calculate(new BigDecimal("100"), variableJackpot("2000")))
                .isEqualByComparingTo("9");
        // growth = 4000 -> decay = 0.04 -> pct = 0.06 -> 0.06 * 100 = 6
        assertThat(strategy.calculate(new BigDecimal("100"), variableJackpot("5000")))
                .isEqualByComparingTo("6");
    }

    @Test
    void percentageNeverDropsBelowFloor() {
        // growth = 100000 -> decay = 1.0 -> raw pct negative, floored at 1% -> 0.01 * 100 = 1
        assertThat(strategy.calculate(new BigDecimal("100"), variableJackpot("101000")))
                .isEqualByComparingTo("1");
    }
}
