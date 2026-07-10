package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.JackpotReward;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JackpotRewardRepositoryTest {

    @Autowired
    private JackpotRewardRepository rewardRepository;

    @Test
    void savesAndFindsRewardByBetId() {
        JackpotReward reward = JackpotReward.builder()
                .betId("bet-42")
                .userId("user-7")
                .jackpotId("jackpot-variable")
                .jackpotRewardAmount(new BigDecimal("5000.0000"))
                .createdAt(Instant.now())
                .build();

        rewardRepository.save(reward);

        Optional<JackpotReward> found = rewardRepository.findByBetId("bet-42");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getJackpotRewardAmount()).isEqualByComparingTo("5000.0000");
        assertThat(found.get().getJackpotId()).isEqualTo("jackpot-variable");
    }

    @Test
    void returnsEmptyForUnknownBetId() {
        assertThat(rewardRepository.findByBetId("nope")).isEmpty();
    }
}
