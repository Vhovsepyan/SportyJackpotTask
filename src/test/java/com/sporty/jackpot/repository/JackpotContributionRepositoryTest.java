package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.JackpotContribution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JackpotContributionRepositoryTest {

    @Autowired
    private JackpotContributionRepository contributionRepository;

    @Test
    void savesAndFindsContributionByBetId() {
        JackpotContribution contribution = JackpotContribution.builder()
                .betId("bet-1")
                .userId("user-1")
                .jackpotId("jackpot-fixed")
                .stakeAmount(new BigDecimal("100.0000"))
                .contributionAmount(new BigDecimal("5.0000"))
                .currentJackpotAmount(new BigDecimal("1005.0000"))
                .createdAt(Instant.now())
                .build();

        contributionRepository.save(contribution);

        Optional<JackpotContribution> found = contributionRepository.findByBetId("bet-1");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getUserId()).isEqualTo("user-1");
        assertThat(found.get().getContributionAmount()).isEqualByComparingTo("5.0000");
        assertThat(found.get().getCurrentJackpotAmount()).isEqualByComparingTo("1005.0000");
    }

    @Test
    void returnsEmptyForUnknownBetId() {
        assertThat(contributionRepository.findByBetId("does-not-exist")).isEmpty();
    }
}
